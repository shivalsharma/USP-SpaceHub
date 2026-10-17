package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.PasswordResetToken;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.repository.UserRepository;
import fj.ac.usp.spacehub.service.AuditService;
import fj.ac.usp.spacehub.service.EmailService;
import fj.ac.usp.spacehub.service.PasswordResetService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class ResetPasswordController {


    /* =========================================================
       LOGGER
       ========================================================= */

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ResetPasswordController.class
            );



    /* =========================================================
       DEPENDENCIES
       ========================================================= */

    private final PasswordResetService passwordResetService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuditService audit;

    private final EmailService emailService;



    /* =========================================================
       RESET PASSWORD PAGE
       GET /reset-password?token=...
       ========================================================= */

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(required = false)
            String token,

            Model model
    ) {


        /* -----------------------------------------------------
           MISSING TOKEN
           ----------------------------------------------------- */

        if (
                token == null
                ||
                token.isBlank()
        ) {


            model.addAttribute(
                    "invalidToken",
                    true
            );


            model.addAttribute(
                    "error",
                    "This password reset link is invalid."
            );


            return "reset-password";
        }



        /* -----------------------------------------------------
           VALIDATE TOKEN
           ----------------------------------------------------- */

        var validToken =
                passwordResetService
                        .findValidToken(
                                token
                        );


        if (
                validToken.isEmpty()
        ) {


            model.addAttribute(
                    "invalidToken",
                    true
            );


            model.addAttribute(
                    "error",
                    "This password reset link is invalid, expired, or has already been used."
            );


            return "reset-password";
        }



        /* -----------------------------------------------------
           VALID TOKEN
           ----------------------------------------------------- */

        model.addAttribute(
                "invalidToken",
                false
        );


        model.addAttribute(
                "token",
                token
        );


        return "reset-password";
    }



    /* =========================================================
       PROCESS NEW PASSWORD
       POST /reset-password
       ========================================================= */

    @PostMapping("/reset-password")
    @Transactional
    public String resetPassword(
            @RequestParam(required = false)
            String token,

            @RequestParam(required = false)
            String newPassword,

            @RequestParam(required = false)
            String confirmPassword,

            Model model,

            RedirectAttributes redirectAttributes
    ) {


        /* -----------------------------------------------------
           VALIDATE TOKEN AGAIN
           ----------------------------------------------------- */

        if (
                token == null
                ||
                token.isBlank()
        ) {


            model.addAttribute(
                    "invalidToken",
                    true
            );


            model.addAttribute(
                    "error",
                    "This password reset link is invalid."
            );


            return "reset-password";
        }



        var validTokenOptional =
                passwordResetService
                        .findValidToken(
                                token
                        );


        if (
                validTokenOptional.isEmpty()
        ) {


            model.addAttribute(
                    "invalidToken",
                    true
            );


            model.addAttribute(
                    "error",
                    "This password reset link is invalid, expired, or has already been used."
            );


            return "reset-password";
        }



        PasswordResetToken resetToken =
                validTokenOptional.get();


        UserAccount user =
                resetToken.getUser();



        /* -----------------------------------------------------
           REQUIRED PASSWORD FIELDS
           ----------------------------------------------------- */

        if (
                newPassword == null
                ||
                newPassword.isBlank()
                ||
                confirmPassword == null
                ||
                confirmPassword.isBlank()
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Please complete both password fields."
            );
        }



        /* -----------------------------------------------------
           PASSWORD CONFIRMATION
           ----------------------------------------------------- */

        if (
                !newPassword.equals(
                        confirmPassword
                )
        ) {


            return showPasswordError(
                    model,
                    token,
                    "New password and confirmation do not match."
            );
        }



        /* -----------------------------------------------------
           NEW PASSWORD MUST DIFFER FROM CURRENT PASSWORD
           ----------------------------------------------------- */

        if (
                passwordEncoder.matches(
                        newPassword,
                        user.getPasswordHash()
                )
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Your new password must be different from your current password."
            );
        }



        /* -----------------------------------------------------
           MINIMUM LENGTH
           ----------------------------------------------------- */

        if (
                newPassword.length()
                <
                8
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Password must contain at least 8 characters."
            );
        }



        /* -----------------------------------------------------
           UPPERCASE
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*[A-Z].*"
                )
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Password must contain at least one uppercase letter."
            );
        }



        /* -----------------------------------------------------
           LOWERCASE
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*[a-z].*"
                )
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Password must contain at least one lowercase letter."
            );
        }



        /* -----------------------------------------------------
           NUMBER
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*\\d.*"
                )
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Password must contain at least one number."
            );
        }



        /* -----------------------------------------------------
           SPECIAL CHARACTER
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*[^A-Za-z0-9].*"
                )
        ) {


            return showPasswordError(
                    model,
                    token,
                    "Password must contain at least one special character."
            );
        }



        /* -----------------------------------------------------
           SAVE NEW BCRYPT PASSWORD
           ----------------------------------------------------- */

        user.setPasswordHash(

                passwordEncoder.encode(
                        newPassword
                )

        );



        /*
         * A password reset gives the account a permanent
         * user-selected password, so any temporary-password
         * requirement should be cleared.
         */
        user.setMustChangePassword(
                false
        );


        userRepository.save(
                user
        );



        /* -----------------------------------------------------
           MARK RESET TOKEN AS USED
           ----------------------------------------------------- */

        passwordResetService.markTokenUsed(
                resetToken
        );



        /* -----------------------------------------------------
           AUDIT LOG
           ----------------------------------------------------- */

        audit.log(
                "PASSWORD_RESET",
                "UserAccount",
                user.getId(),
                "Password reset completed through secure reset link"
        );



        /* -----------------------------------------------------
           PASSWORD CHANGED EMAIL
           ----------------------------------------------------- */

        /*
         * The password reset should remain successful even if
         * the security-notification email cannot be delivered.
         */
        try {

            emailService.sendPasswordChangedEmail(
                    user
            );

        } catch (Exception exception) {

            LOGGER.error(
                    "Password changed notification email could not be sent.",
                    exception
            );

        }



        /* -----------------------------------------------------
           SUCCESS
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(

                "success",

                "Your password has been reset successfully. "
                        +
                "You can now sign in with your new password."

        );


        return "redirect:/login";
    }



    /* =========================================================
       SHOW PASSWORD VALIDATION ERROR
       ========================================================= */

    private String showPasswordError(
            Model model,
            String token,
            String errorMessage
    ) {


        model.addAttribute(
                "invalidToken",
                false
        );


        model.addAttribute(
                "token",
                token
        );


        model.addAttribute(
                "error",
                errorMessage
        );


        return "reset-password";
    }

}