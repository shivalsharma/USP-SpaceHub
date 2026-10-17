package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
                @Index(
                        name = "idx_password_reset_token_hash",
                        columnList = "token_hash"
                ),
                @Index(
                        name = "idx_password_reset_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {


    /* =========================================================
       PRIMARY KEY
       ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /* =========================================================
       USER
       ========================================================= */

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserAccount user;



    /* =========================================================
       TOKEN HASH
       ========================================================= */

    /*
     * IMPORTANT:
     *
     * The actual reset token sent by email is NOT stored
     * in the database.
     *
     * Only its SHA-256 hash is stored here.
     *
     * A SHA-256 hexadecimal hash is 64 characters long.
     */
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;



    /* =========================================================
       EXPIRY
       ========================================================= */

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;



    /* =========================================================
       USED STATUS
       ========================================================= */

    /*
     * false = token can still potentially be used
     * true  = token has already been consumed
     */
    @Column(nullable = false)
    private boolean used = false;



    /* =========================================================
       USED DATE
       ========================================================= */

    @Column(name = "used_at")
    private LocalDateTime usedAt;



    /* =========================================================
       CREATED DATE
       ========================================================= */

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt =
            LocalDateTime.now();

}