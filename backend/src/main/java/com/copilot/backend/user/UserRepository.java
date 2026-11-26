package com.copilot.backend.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring сам перетворить це на: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Перевірка при реєстрації, чи не зайнятий email
    boolean existsByEmail(String email);
}