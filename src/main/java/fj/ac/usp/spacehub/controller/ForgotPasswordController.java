package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.repository.UserRepository;
import fj.ac.usp.spacehub.service.EmailService;
import fj.ac.usp.spacehub.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {


    /* =========================================================
       LOGGER
       ========================================================= */

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ForgotPasswordController.class
            );



    /* =========================================================
       DEPENDENCIES
       ========================================================= */

    private final UserRepository userRepository;

    private final PasswordResetService passwordResetService;

    private final EmailService emailService;



    /* =========================================================
       FORGOT PASSWORD PAGE
       GET /forgot-password
       ========================================================= */

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(
            Model model
    ) {


        return "forgot-password";
    }



    /* =========================================================
       PROCESS FORGOT PASSWORD
       POST /forgot-password
       ========================================================= */

    @PostMapping("/forgot-password")
    public String requestPasswordReset(
            @RequestParam(required = false)
            String email,

            RedirectAttributes redirectAttributes
    ) {


        /* -----------------------------------------------------
           VALIDATE EMAIL FIELD
           ----------------------------------------------------- */

        if (
                email == null
                ||
                email.trim().isEmpty()
        ) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please enter your email address."
            );


            return "redirect:/forgot-password";
        }



        /* -----------------------------------------------------
           NORMALIZE EMAIL
           ----------------------------------------------------- */

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase();



        /* -----------------------------------------------------
           LOOK UP USER
           ----------------------------------------------------- */

        userRepository
                .findByEmailIgnoreCase(
                        normalizedEmail
                )

                /*
                 * Only active accounts should receive
                 * password-reset links.
                 */
                .filter(
                        UserAccount::isActive
                )

                .ifPresent(
                        user -> {


                            try {


                                /* ---------------------------------
                                   CREATE SECURE RESET TOKEN
                                   --------------------------------- */

                                String resetToken =
                                        passwordResetService
                                                .createResetToken(
                                                        user
                                                );



                                /* ---------------------------------
                                   SEND RESET EMAIL
                                   --------------------------------- */

                                emailService
                                        .sendPasswordResetEmail(
                                                user,
                                                resetToken
                                        );


                            } catch (Exception exception) {


                                /*
                                 * Do not expose SMTP/server
                                 * information to the browser.
                                 *
                                 * Log the technical problem only.
                                 */
                                LOGGER.error(
                                        "Password reset email could not be sent.",
                                        exception
                                );

                            }

                        }
                );



        /* -----------------------------------------------------
           GENERIC RESPONSE
           ----------------------------------------------------- */

        /*
         * IMPORTANT SECURITY BEHAVIOUR:
         *
         * We deliberately show the SAME message whether or not
         * the email exists.
         *
         * Otherwise an attacker could use the Forgot Password
         * page to discover which email addresses have SpaceHub
         * accounts.
         */
        redirectAttributes.addFlashAttribute(

                "success",

                "If an active SpaceHub account exists for that email address, "
                        +
                "a password reset link has been sent."

        );


        return "redirect:/forgot-password";
    }

}