package fj.ac.usp.spacehub.service;

import fj.ac.usp.spacehub.model.PasswordResetToken;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.repository.PasswordResetTokenRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.time.LocalDateTime;

import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PasswordResetService {


    /* =========================================================
       DEPENDENCY
       ========================================================= */

    private final PasswordResetTokenRepository tokenRepository;



    /* =========================================================
       TOKEN SETTINGS
       ========================================================= */

    /*
     * 32 random bytes = 256 bits of entropy.
     */
    private static final int TOKEN_BYTE_LENGTH = 32;


    /*
     * Reset links remain valid for 30 minutes.
     */
    private static final int TOKEN_EXPIRY_MINUTES = 30;


    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();



    /* =========================================================
       CREATE RESET TOKEN
       ========================================================= */

    @Transactional
    public String createResetToken(
            UserAccount user
    ) {


        if (
                user == null
                ||
                user.getId() == null
        ) {

            throw new IllegalArgumentException(
                    "A valid user account is required."
            );

        }



        /* -----------------------------------------------------
           REMOVE PREVIOUS RESET TOKENS FOR THIS USER
           ----------------------------------------------------- */

        /*
         * Only the newest reset request should remain valid.
         */
        tokenRepository.deleteByUserId(
                user.getId()
        );



        /* -----------------------------------------------------
           GENERATE RANDOM TOKEN
           ----------------------------------------------------- */

        byte[] randomBytes =
                new byte[TOKEN_BYTE_LENGTH];


        SECURE_RANDOM.nextBytes(
                randomBytes
        );


        /*
         * URL-safe encoding means the token can safely appear
         * inside a password-reset URL.
         */
        String rawToken =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                randomBytes
                        );



        /* -----------------------------------------------------
           HASH TOKEN
           ----------------------------------------------------- */

        String tokenHash =
                hashToken(
                        rawToken
                );



        /* -----------------------------------------------------
           CREATE DATABASE RECORD
           ----------------------------------------------------- */

        PasswordResetToken resetToken =
                new PasswordResetToken();


        resetToken.setUser(
                user
        );


        resetToken.setTokenHash(
                tokenHash
        );


        resetToken.setExpiresAt(
                LocalDateTime
                        .now()
                        .plusMinutes(
                                TOKEN_EXPIRY_MINUTES
                        )
        );


        resetToken.setUsed(
                false
        );



        /* -----------------------------------------------------
           SAVE HASH ONLY
           ----------------------------------------------------- */

        tokenRepository.save(
                resetToken
        );



        /*
         * IMPORTANT:
         *
         * We return the original token so it can be placed in
         * the email reset link.
         *
         * The database stores only tokenHash.
         */
        return rawToken;
    }



    /* =========================================================
       FIND VALID RESET TOKEN
       ========================================================= */

    public Optional<PasswordResetToken> findValidToken(
            String rawToken
    ) {


        if (
                rawToken == null
                ||
                rawToken.isBlank()
        ) {

            return Optional.empty();
        }


        String tokenHash =
                hashToken(
                        rawToken
                );


        return tokenRepository
                .findByTokenHash(
                        tokenHash
                )
                .filter(
                        token ->
                                !token.isUsed()
                )
                .filter(
                        token ->
                                token
                                        .getExpiresAt()
                                        .isAfter(
                                                LocalDateTime.now()
                                        )
                );
    }



    /* =========================================================
       MARK TOKEN AS USED
       ========================================================= */

    @Transactional
    public void markTokenUsed(
            PasswordResetToken token
    ) {


        if (token == null) {

            throw new IllegalArgumentException(
                    "Password reset token is required."
            );

        }


        token.setUsed(
                true
        );


        token.setUsedAt(
                LocalDateTime.now()
        );


        tokenRepository.save(
                token
        );

    }



    /* =========================================================
       DELETE EXPIRED TOKENS
       ========================================================= */

    @Transactional
    public void deleteExpiredTokens() {


        tokenRepository.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );

    }



    /* =========================================================
       SHA-256 TOKEN HASH
       ========================================================= */

    private String hashToken(
            String rawToken
    ) {


        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            /*
             * SHA-256 = 32 bytes = 64 hexadecimal characters.
             *
             * This matches token_hash VARCHAR(64).
             */
            return HexFormat
                    .of()
                    .formatHex(
                            hash
                    );


        } catch (NoSuchAlgorithmException exception) {

            /*
             * SHA-256 is required by the Java platform.
             * If unavailable, the application cannot safely
             * create password-reset tokens.
             */
            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );

        }

    }

}