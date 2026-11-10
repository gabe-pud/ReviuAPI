package br.edu.fatecpg.reviu.controllers;

import br.edu.fatecpg.reviu.domain.user.User;
import br.edu.fatecpg.reviu.dto.requests.DeckRequestDTO;
import br.edu.fatecpg.reviu.dto.responses.DeckResponseDTO;
import br.edu.fatecpg.reviu.services.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
@Tag(
        name = "Decks",
        description = "Endpoints responsáveis pela criação, gerenciamento e listagem de decks de estudo."
)
public class DeckController {
    private final DeckService deckService;

    @PostMapping
    @Operation(
            summary = "Cria um novo deck",
            description = "Cria um novo deck para o usuário autenticado e retorna os dados do deck criado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deck criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DeckResponseDTO> createDeck(@AuthenticationPrincipal User user, @RequestBody DeckRequestDTO request){

        DeckResponseDTO newDeck = deckService.createDeck(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDeck);
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os decks do usuário",
            description = "Retorna todos os decks criados pelo usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de decks retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum deck encontrado para o usuário")
    })
    public ResponseEntity<List<DeckResponseDTO>> getDeckByUser(@AuthenticationPrincipal User user){

        List<DeckResponseDTO> decks= deckService.getDeckByUser(user.getId());
        return ResponseEntity.ok(decks);
    }

    @PutMapping("/{deckId}")
    @Operation(
            summary = "Atualiza um deck",
            description = "Atualiza os dados de um deck específico do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deck atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Deck não encontrado")
    })
    public ResponseEntity<DeckResponseDTO> updateDeck(@AuthenticationPrincipal User user, @PathVariable Long deckId, @RequestBody DeckRequestDTO request){

        DeckResponseDTO updateDeck = deckService.updateDeck(user.getId(), deckId, request);
        return ResponseEntity.ok(updateDeck);
    }

    @DeleteMapping("/{deckId}")
    @Operation(
            summary = "Remove um deck",
            description = "Exclui permanentemente um deck do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deck removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Deck não encontrado")
    })
    public ResponseEntity<Void> deleteDeck(@AuthenticationPrincipal User user, @PathVariable Long deckId){

        deckService.deleteDeck(user.getId(), deckId);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{deckId}/members/{memberId}")
    @Operation(summary = "Cria um deck para outro usuário usando as mesmas cartas")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deck compartilhado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Deck ou usuário destinatário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Usuário autenticado não é o proprietário do deck")
    })
    public ResponseEntity<Void> shareDeck(
            @AuthenticationPrincipal User user,
            @PathVariable Long deckId,
            @PathVariable Long memberId
    ) {
        deckService.shareDeck(user.getId(), deckId, memberId);
        return ResponseEntity.noContent().build();
    }
}
