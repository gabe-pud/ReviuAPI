package br.edu.fatecpg.reviu.domain.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "verification_codes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationCodes {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (nullable = true)
    private String code;
    @Column(nullable = true)
    private Instant expiry;
    
    @OneToOne 
    @JoinColumn (name = "user_id")
    private User user;
}