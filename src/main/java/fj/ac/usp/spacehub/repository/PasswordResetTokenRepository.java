package fj.ac.usp.spacehub.repository;

import fj.ac.usp.spacehub.model.PasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;


public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {


    /* =========================================================
       FIND TOKEN
       ========================================================= */

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );



    /* =========================================================
       DELETE OLD TOKENS FOR USER
       ========================================================= */

    void deleteByUserId(
            Long userId
    );



    /* =========================================================
       DELETE EXPIRED TOKENS
       ========================================================= */

    void deleteByExpiresAtBefore(
            LocalDateTime dateTime
    );

}