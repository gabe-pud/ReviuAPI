package br.edu.fatecpg.reviu.dto.responses;

import br.edu.fatecpg.reviu.domain.deck.DeckShareRequest;

import java.time.Instant;

public record DeckShareRequestResponseDTO(
        Long id,
        Long deckId,
        String deckName,
        Long senderId,
        String senderName,
        String status,
        Instant createdAt
) {
    public DeckShareRequestResponseDTO(DeckShareRequest request) {
        this(
                request.getId(),
                request.getDeck().getId(),
                request.getDeck().getName(),
                request.getSender().getId(),
                request.getSender().getName(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
