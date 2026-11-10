package br.edu.fatecpg.reviu.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DeckRequestDTO", description = "DTO para criação ou atualização de um deck")
public record DeckRequestDTO(

        @Schema(description = "ID do deck (utilizado apenas em atualizações)", example = "1")
        Long id,

        @Schema(description = "Nome do deck", example = "Deck de Matemática")
        String name,

        @Schema(description = "Cor do deck", example = "#4F46E5")
        String color

) {}
