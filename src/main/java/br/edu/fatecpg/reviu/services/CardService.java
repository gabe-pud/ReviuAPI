package br.edu.fatecpg.reviu.services;

import br.edu.fatecpg.reviu.domain.card.Card;
import br.edu.fatecpg.reviu.domain.card.CardReview;
import br.edu.fatecpg.reviu.domain.deck.Deck;
import br.edu.fatecpg.reviu.domain.user.User;
import br.edu.fatecpg.reviu.dto.requests.CardRequestDTO;
import br.edu.fatecpg.reviu.integration.DictionaryAPIService;
import br.edu.fatecpg.reviu.repositories.CardRepository;
import br.edu.fatecpg.reviu.repositories.CardReviewRepository;
import br.edu.fatecpg.reviu.repositories.DeckRepository;
import br.edu.fatecpg.reviu.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardReviewRepository cardReviewRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final DictionaryAPIService dictionaryAPIService;

    private Card buildCard(CardRequestDTO request) {
        Card card = new Card();
        card.setFrontText(request.frontText());
        card.setBackText(request.backText());
        card.setImageUrl(request.imageUrl());
        card.setAudioUrl(request.audioUrl());

        if (request.audioUrl() == null && isValidDictionaryWord(request.frontText())) {
            card.setAudioUrl(dictionaryAPIService.getFirstAudioUrl(request.frontText().trim()));
        }
        return card;
    }

    @Transactional
    public Card createCard(Long deckId, Long userId, CardRequestDTO request) {
        Deck deck = findDeck(deckId);
        Card card = cardRepository.save(buildCard(request));
        deck.getCards().add(card);
        deckRepository.save(deck);
        createReviewIfMissing(userId, card);
        return card;
    }

    @Transactional
    public List<Card> createManyCards(Long deckId, Long userId, List<CardRequestDTO> requests) {
        Deck deck = findDeck(deckId);
        List<Card> cards = cardRepository.saveAll(requests.stream().map(this::buildCard).toList());
        deck.getCards().addAll(cards);
        deckRepository.save(deck);
        cards.forEach(card -> createReviewIfMissing(userId, card));
        return cards;
    }

    public List<Card> getCardByDeck(Long deckId) {
        findDeck(deckId);
        return cardRepository.findByDeckId(deckId);
    }

    @Transactional
    public Card updateCard(Long deckId, Long cardId, CardRequestDTO request) {
        Deck deck = findDeck(deckId);
        Card card = findCard(cardId);
        ensureInDeck(deck, card);

        if (request.frontText() != null) card.setFrontText(request.frontText());
        if (request.backText() != null) card.setBackText(request.backText());
        if (request.imageUrl() != null) card.setImageUrl(request.imageUrl());
        if (request.audioUrl() != null) {
            card.setAudioUrl(request.audioUrl());
        } else if (request.frontText() != null && isValidDictionaryWord(request.frontText())) {
            card.setAudioUrl(dictionaryAPIService.getFirstAudioUrl(request.frontText().trim()));
        }
        return cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long deckId, Long cardId) {
        Deck deck = findDeck(deckId);
        Card card = findCard(cardId);
        ensureInDeck(deck, card);
        deck.getCards().removeIf(item -> item.getId().equals(cardId));
        deckRepository.save(deck);
    }

    @Transactional
    public CardReview reviewCard(Long cardId, Long userId, int quality) {
        if (quality < 0 || quality > 5) throw new IllegalArgumentException("Quality must be between 0 and 5");
        Card card = findCard(cardId);
        CardReview review = cardReviewRepository.findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> createReviewIfMissing(userId, card));
        processReview(review, quality);
        return cardReviewRepository.save(review);
    }

    @Transactional
    public List<Card> getDueCards(Long deckId, Long userId) {
        List<Card> cards = getCardByDeck(deckId);
        List<Long> cardIds = cards.stream().map(Card::getId).toList();
        cards.forEach(card -> createReviewIfMissing(userId, card));
        List<CardReview> dueReviews = cardReviewRepository
                .findByUserIdAndCardIdInAndNextReviewLessThanEqual(userId, cardIds, LocalDate.now());
        return dueReviews.stream().map(CardReview::getCard).toList();
    }

    public CardReview getReview(Long cardId, Long userId) {
        return cardReviewRepository.findByUserIdAndCardId(userId, cardId).orElse(null);
    }

    private void processReview(CardReview review, int quality) {
        double ef = review.getEasinessFactor();
        int repetitions = review.getRepetitions();
        int interval = review.getInterval();

        if (quality < 3) {
            repetitions = 0;
            interval = 1;
        } else {
            repetitions++;
            if (repetitions == 1) interval = 1;
            else if (repetitions == 2) interval = 6;
            else interval = (int) Math.round(interval * ef);
            ef = Math.max(1.3, ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
        }

        review.setRepetitions(repetitions);
        review.setInterval(interval);
        review.setEasinessFactor(ef);
        review.setLastReview(LocalDate.now());
        review.setNextReview(LocalDate.now().plusDays(interval));
    }

    private CardReview createReviewIfMissing(Long userId, Card card) {
        return cardReviewRepository.findByUserIdAndCardId(userId, card.getId()).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            CardReview review = new CardReview();
            review.setUser(user);
            review.setCard(card);
            review.setInterval(1);
            review.setEasinessFactor(2.5);
            review.setNextReview(LocalDate.now());
            return cardReviewRepository.save(review);
        });
    }

    private Deck findDeck(Long deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new EntityNotFoundException("Deck not found"));
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
    }

    private void ensureInDeck(Deck deck, Card card) {
        if (deck.getCards().stream().noneMatch(item -> item.getId().equals(card.getId()))) {
            throw new IllegalArgumentException("This card does not belong to this deck");
        }
    }

    private boolean isValidDictionaryWord(String word) {
        return word != null && !word.isBlank() && !word.trim().contains(" ")
                && word.trim().matches("^[a-zA-Z]+$");
    }
}
