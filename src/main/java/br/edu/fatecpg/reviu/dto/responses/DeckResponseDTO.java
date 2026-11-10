package br.edu.fatecpg.reviu.dto.responses;

import br.edu.fatecpg.reviu.domain.deck.Deck;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "DeckResponseDTO", description = "DTO de resposta para envio dos dados de um deck, incluindo seus cards")
public record DeckResponseDTO(

        @Schema(description = "ID único do deck", example = "1")
        Long id,

        @Schema(description = "Nome do deck", example = "Deck de Matemática")
        String name,

        @Schema(description = "Cor do deck", example = "#4F46E5")
        String color,

        @Schema(description = "Lista de cards pertencentes ao deck")
        List<CardResponseDTO> cards

) {
    public DeckResponseDTO(Deck deck){
        this(
                deck.getId(),
                deck.getName(),
                deck.getColor(),
                deck.getCards() != null
                        ? deck.getCards().stream().map(CardResponseDTO::new).toList()
                        : List.of()
        );
    }
}
