package br.edu.fatecpg.reviu.domain.card;

import br.edu.fatecpg.reviu.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "card_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_card_reviews_user_card", columnNames = {"user_id", "card_id"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_id")
    private Card card;

    private int repetitions;
    private int interval;
    private double easinessFactor;
    private LocalDate lastReview;
    private LocalDate nextReview;
}
