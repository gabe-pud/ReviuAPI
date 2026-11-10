package br.edu.fatecpg.reviu.repositories;

import br.edu.fatecpg.reviu.domain.card.CardReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardReviewRepository extends JpaRepository<CardReview, Long> {
    Optional<CardReview> findByUserIdAndCardId(Long userId, Long cardId);
    List<CardReview> findByUserIdAndCardIdIn(Long userId, List<Long> cardIds);
    List<CardReview> findByUserIdAndCardIdInAndNextReviewLessThanEqual(
            Long userId, List<Long> cardIds, LocalDate date);
    void deleteByCardId(Long cardId);
}
