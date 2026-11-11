package br.edu.fatecpg.reviu.repositories;

import br.edu.fatecpg.reviu.domain.user.UserOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserOnboardingRepository extends JpaRepository<UserOnboarding, Long> {
    Optional<UserOnboarding> findByUserId(Long userId);
}