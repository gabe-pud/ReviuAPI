package br.edu.fatecpg.reviu.dto.responses;

import br.edu.fatecpg.reviu.domain.card.Card;
import br.edu.fatecpg.reviu.domain.card.CardReview;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "CardResponseDTO", description = "DTO de resposta para envio dos dados de um card")
public record CardResponseDTO(

        @Schema(description = "ID único do card", example = "1")
        Long id,

        @Schema(description = "Texto que aparece na frente do card", example = "O que é Java?")
        String frontText,

        @Schema(description = "Texto que aparece no verso do card", example = "Uma linguagem de programação orientada a objetos.")
        String backText,

        @Schema(description = "Número de repetições do card no sistema SM2", example = "3")
        int repetition,

        @Schema(description = "Intervalo atual de revisão do card em dias", example = "5")
        int interval,

        @Schema(description = "Fator de facilidade do card (Easiness Factor) usado no SM2", example = "2.5")
        double easinessFactor,

        @Schema(description = "Data da próxima revisão do card", example = "2025-11-25")
        LocalDate nextReview,

        @Schema(description = "URL da imagem associada ao card (opcional)", example = "https://minhaapi.com/imagens/card1.png")
        String imageUrl,

        @Schema(description = "URL do áudio associado ao card (opcional)", example = "https://minhaapi.com/audios/card1.mp3")
        String audioUrl

) {
    public CardResponseDTO(Card card){
        this(card, null);
    }

    public CardResponseDTO(Card card, CardReview review){
        this(
                card.getId(),
                card.getFrontText(),
                card.getBackText(),
                review != null ? review.getRepetitions() : 0,
                review != null ? review.getInterval() : 1,
                review != null ? review.getEasinessFactor() : 2.5,
                review != null ? review.getNextReview() : null,
                card.getImageUrl(),
                card.getAudioUrl()
        );
    }
}
