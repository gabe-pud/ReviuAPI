package br.edu.fatecpg.reviu.services;

import br.edu.fatecpg.reviu.domain.deck.Deck;
import br.edu.fatecpg.reviu.domain.user.User;
import br.edu.fatecpg.reviu.dto.requests.DeckRequestDTO;
import br.edu.fatecpg.reviu.dto.responses.DeckResponseDTO;
import br.edu.fatecpg.reviu.repositories.DeckRepository;
import br.edu.fatecpg.reviu.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeckService {
    public final DeckRepository deckRepository;
    public final UserRepository userRepository;

    //CREATE
    public DeckResponseDTO createDeck(Long userId, DeckRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Deck deck = new Deck();
        deck.setName(request.name());
        deck.setColor(request.color());
        deck.setUser(user);

        deckRepository.save(deck);
        return new DeckResponseDTO(deck.getId(), deck.getName(), deck.getColor(), null);
    }

    //VIEW
    public List<DeckResponseDTO> getDeckByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Deck> decks = deckRepository.findByUserId(userId);
        return decks.stream()
                .map(DeckResponseDTO::new)
                .toList();
    }

    //UPDATE
    public DeckResponseDTO updateDeck(Long userId, Long deckId, DeckRequestDTO request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (!deck.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("This deck does not belong to this user");
        }

        deck.setName(request.name());
        deck.setColor(request.color());
        deckRepository.save(deck);
        return new DeckResponseDTO(deck);
    }

    //DELETE
    public void deleteDeck(Long userId, Long deckId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (!deck.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("This deck does not belong to this user");
        }

        deckRepository.delete(deck);
    }

    @Transactional
    public void shareDeck(Long ownerId, Long deckId, Long memberId) {
        Deck sourceDeck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));
        if (!sourceDeck.getUser().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the deck owner can share it");
        }
        User recipient = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Deck sharedDeck = new Deck();
        sharedDeck.setName(sourceDeck.getName());
        sharedDeck.setColor(sourceDeck.getColor());
        sharedDeck.setUser(recipient);
        sharedDeck.getCards().addAll(sourceDeck.getCards());
        deckRepository.save(sharedDeck);
    }
}
