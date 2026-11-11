package br.edu.fatecpg.reviu.repositories;

import br.edu.fatecpg.reviu.domain.deck.DeckShareRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckShareRequestRepository extends JpaRepository<DeckShareRequest, Long> {
    List<DeckShareRequest> findByRecipientIdAndStatusOrderByCreatedAtDesc(Long recipientId, String status);

    Optional<DeckShareRequest> findByIdAndRecipientIdAndStatus(
            Long id,
            Long recipientId,
            String status
    );

    boolean existsByDeckIdAndRecipientIdAndStatus(Long deckId, Long recipientId, String status);
}
