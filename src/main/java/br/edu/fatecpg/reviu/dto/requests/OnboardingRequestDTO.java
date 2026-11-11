package br.edu.fatecpg.reviu.dto.requests;

public record OnboardingRequestDTO(
        String focoEstudos,
        String prazoFinal,
        String metaDiaria,
        String horarioFoco,
        String retencaoConteudo,
        String ritmoRevisao
) {}