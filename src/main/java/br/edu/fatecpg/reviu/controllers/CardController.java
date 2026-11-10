package br.edu.fatecpg.reviu.controllers;

import br.edu.fatecpg.reviu.domain.card.Card;
import br.edu.fatecpg.reviu.dto.requests.CardRequestDTO;
import br.edu.fatecpg.reviu.dto.requests.ReviewRequestDTO;
import br.edu.fatecpg.reviu.dto.responses.CardResponseDTO;
import br.edu.fatecpg.reviu.dto.responses.UploadResponseDTO;
import br.edu.fatecpg.reviu.services.CardService;
import br.edu.fatecpg.reviu.services.GeminiAiService;
import br.edu.fatecpg.reviu.services.PDFService;
import br.edu.fatecpg.reviu.services.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import br.edu.fatecpg.reviu.domain.user.User;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/decks/{deckId}/cards")
@RequiredArgsConstructor
@Tag(
        name = "Cards",
        description = "Endpoints responsáveis pela criação, gerenciamento e revisão de cards dentro de um deck."
)
public class CardController {
    private final CardService cardService;
    private final UploadService  uploadService;
    private final GeminiAiService geminiAiService;
    private final PDFService pdfService;

    @PostMapping
    @Operation(
            summary = "Cria um novo card",
            description = "Cria e adiciona um card ao deck informado pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Card criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Deck não encontrado")
    })
    public ResponseEntity<CardResponseDTO> createCard(@PathVariable Long deckId, @AuthenticationPrincipal User user, @RequestBody CardRequestDTO request){

        Card newCard = cardService.createCard(deckId, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CardResponseDTO(newCard, cardService.getReview(newCard.getId(), user.getId())));
    }

    @GetMapping
    @Operation(
            summary = "Lista os cards de um deck",
            description = "Retorna todos os cards pertencentes ao deck informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Deck não encontrado")
    })
    public ResponseEntity<List<CardResponseDTO>> getCardByDeck(@PathVariable Long deckId, @AuthenticationPrincipal User user){

        List<Card> cards= cardService.getCardByDeck(deckId);
        List<CardResponseDTO> response= cards.stream()
                .map(card -> new CardResponseDTO(card, cardService.getReview(card.getId(), user.getId()))).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cardId}")
    @Operation(
            summary = "Atualiza um card",
            description = "Atualiza o conteúdo de um card específico dentro de um deck."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Deck ou card não encontrado")
    })
    public ResponseEntity<CardResponseDTO> updateCard(@PathVariable Long deckId, @PathVariable Long cardId, @AuthenticationPrincipal User user, @RequestBody CardRequestDTO request){

        Card updateCard = cardService.updateCard(deckId, cardId, request);
        return ResponseEntity.ok(new CardResponseDTO(updateCard, cardService.getReview(cardId, user.getId())));
    }

    @DeleteMapping("/{cardId}")
    @Operation(
            summary = "Remove um card",
            description = "Remove a associação do card com o deck, preservando o card para outros decks compartilhados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Deck ou card não encontrado")
    })
    public ResponseEntity<Void> deleteCard(@PathVariable Long deckId, @PathVariable Long cardId){

        cardService.deleteCard(deckId, cardId);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{cardId}/review")
    @Operation(
            summary = "Registra uma revisão do card (SM2)",
            description = "Aplica o algoritmo SM2 para atualizar a dificuldade, intervalo e próximo agendamento de revisão do card."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revisão registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nota de qualidade inválida"),
            @ApiResponse(responseCode = "404", description = "Card não encontrado")
    })
    public ResponseEntity<CardResponseDTO> reviewCard(@PathVariable Long cardId, @AuthenticationPrincipal User user, @RequestBody ReviewRequestDTO request){

        var review = cardService.reviewCard(cardId, user.getId(), request.quality());
        return ResponseEntity.ok(new CardResponseDTO(review.getCard(), review));
    }

    @GetMapping("/due")
    @Operation(
            summary = "Retorna os cards de revisão pendente",
            description = "Lista todos os cards do deck cujo prazo de revisão está vencido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Deck não encontrado")
    })
    public ResponseEntity<List<CardResponseDTO>> getDueCards(@PathVariable Long deckId, @AuthenticationPrincipal User user){

       List<Card> dueCards = cardService.getDueCards(deckId, user.getId());
       return ResponseEntity.ok(dueCards.stream()
               .map(card -> new CardResponseDTO(card, cardService.getReview(card.getId(), user.getId()))).toList());
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(
            summary = "Realiza upload de arquivos",
            description = "Envia uma imagem ou áudio e retorna a URL gerada no armazenamento em nuvem."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "500", description = "Erro ao fazer upload")
    })
    public ResponseEntity<UploadResponseDTO> upload(@RequestParam MultipartFile file) throws IOException {
        String url = uploadService.uploadFile(file);
        return ResponseEntity.ok(new UploadResponseDTO(url));
    }

    @PostMapping("/bulk")
    @Operation(
            summary = "Cria múltiplas cartas de uma vez",
            description = "Recebe uma lista de cartas e as adiciona ao deck especificado pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cartas criadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou deck inexistente")
    })
    public ResponseEntity<List<CardResponseDTO>> createMany(
            @PathVariable Long deckId,
            @AuthenticationPrincipal User user,
            @RequestBody List<CardRequestDTO> requests
    ) {
        List<Card> cards = cardService.createManyCards(deckId, user.getId(), requests);
        List<CardResponseDTO> response= cards.stream()
                .map(card -> new CardResponseDTO(card, cardService.getReview(card.getId(), user.getId()))).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-from-file")
    @Operation(
            summary = "Gera cartas a partir de um arquivo PDF",
            description = "Extrai o texto de um PDF enviado e gera cartas automaticamente utilizando IA."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Arquivo PDF contendo o conteúdo para gerar cartas.",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/pdf",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cartas geradas com sucesso a partir do PDF"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou falha na extração do texto")
    })
    public ResponseEntity<String> generateFromPdf(@RequestParam("file") MultipartFile file) {
        String text = pdfService.extractText(file);
        String jsonCards = geminiAiService.createCards(text);

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(jsonCards);
    }
}
