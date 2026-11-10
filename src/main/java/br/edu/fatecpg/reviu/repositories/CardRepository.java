package br.edu.fatecpg.reviu.repositories;

import br.edu.fatecpg.reviu.domain.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("select c from Card c join c.decks d where d.id = :deckId")
    List<Card> findByDeckId(@Param("deckId") Long deckId);

}
