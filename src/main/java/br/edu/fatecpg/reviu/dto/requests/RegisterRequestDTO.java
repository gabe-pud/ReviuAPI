package br.edu.fatecpg.reviu.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequestDTO", description = "DTO para registro de um novo usuário")
public record RegisterRequestDTO(

        @Schema(description = "Nome completo do usuário", example = "Thiago Almeida")
        String name,

        @Schema(description = "Nome de usuário único", example = "thi_almeida")
        String username,

        @Schema(description = "E-mail do usuário", example = "usuario@exemplo.com")
        String email,

        @Schema(
                description = "Senha do usuário. Deve ter no mínimo 8 caracteres, conter letras, números e pelo menos um caractere especial.",
                example = "SenhaForte123!"
        )
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&.])[A-Za-z\\d@$!%*#?&.]{8,}$",
                message = "A senha deve conter letras, números e caracteres especiais."
        )
        String password,

        @Schema(
                description = "Área principal de estudo do usuário, como foco geral dos conteúdos a serem revisados.",
                example = "Vestibular / ENEM",
                defaultValue = "Vestibular / ENEM"
        )
        String focoEstudos,

        @Schema(
                description = "Prazo estimado para a conclusão dos estudos ou metas do usuário.",
                example = "Próximos 3 meses",
                defaultValue = "Próximos 3 meses"
        )
        String prazoFinal,

        @Schema(
                description = "Meta diária de estudo definida pelo usuário para manter a rotina de revisão.",
                example = "10 minutos",
                defaultValue = "10 minutos"
        )
        String metaDiaria,

        @Schema(
                description = "Horário preferido para o foco em estudos e revisão de conteúdo.",
                example = "Manhã",
                defaultValue = "Manhã"
        )
        String horarioFoco,

        @Schema(
                description = "Nível de retenção do conteúdo informado pelo usuário para personalizar o aprendizado.",
                example = "Esqueço fácil",
                defaultValue = "Esqueço fácil"
        )
        String retencaoConteudo,

        @Schema(
                description = "Intensidade do ritmo de revisão escolhido pelo usuário para o planejamento de estudos.",
                example = "Leve",
                defaultValue = "Leve"
        )
        String ritmoRevisao
) {}
