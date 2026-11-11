package br.edu.fatecpg.reviu.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ReviewRequestDTO",
        description = "DTO para enviar a nota da revisão. O intervalo é previsto pela API de ML, com fallback para SM-2."
)
public record ReviewRequestDTO(

        @Schema(
                description = "Nota da revisão do card (0 a 5), enviada à previsão de ML e usada pelo SM-2 caso a previsão falhe.",
                example = "4"
        )
        int quality

) {}
