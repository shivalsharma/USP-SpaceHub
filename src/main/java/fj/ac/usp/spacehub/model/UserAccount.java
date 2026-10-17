package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserAccount {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            nullable = false,
            unique = true,
            length = 40
    )
    private String userCode;


    @Column(nullable = false)
    private String name;


    @Column(
            nullable = false,
            unique = true
    )
    private String email;


    @Column(nullable = false)
    private String passwordHash;


    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private UserRole role;


    @Column(nullable = false)
    private boolean active = true;


    private String department;


    private Integer maxDailyBookings;


    private Integer maxWeeklyBookings;



    /* =========================================================
       PROFILE PICTURE
       ========================================================= */

    /*
     * Stores only the uploaded profile picture filename.
     *
     * Example:
     * 8f43b9a2-profile.jpg
     *
     * The actual image is stored in:
     * uploads/profile-pictures/
     */
    @Column(length = 255)
    private String profilePicture;



    /* =========================================================
       TEMPORARY PASSWORD STATUS
       ========================================================= */

    /*
     * false:
     * User has a normal permanent password.
     *
     * true:
     * User is currently using a temporary password and must
     * change it before continuing to use SpaceHub normally.
     *
     * Existing accounts remain false because the MySQL column
     * was created with DEFAULT 0.
     */
    @Column(
            name = "must_change_password",
            nullable = false
    )
    private boolean mustChangePassword = false;



    /* =========================================================
       CREATED / UPDATED DATES
       ========================================================= */

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt =
            LocalDateTime.now();


    private LocalDateTime updatedAt =
            LocalDateTime.now();



    /* =========================================================
       UPDATE TIMESTAMP
       ========================================================= */

    @PreUpdate
    void touch() {

        updatedAt =
                LocalDateTime.now();

    }

}