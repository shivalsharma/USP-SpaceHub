package fj.ac.usp.spacehub.service;

import fj.ac.usp.spacehub.model.UserAccount;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;


    @Value("${spacehub.mail.from}")
    private String fromAddress;


    @Value("${spacehub.base-url:http://localhost:8080}")
    private String baseUrl;



    /* =========================================================
       NEW USER / WELCOME EMAIL
       ========================================================= */

    public void sendNewAccountEmail(
            UserAccount user,
            String temporaryPassword
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setFrom(
                fromAddress
        );


        message.setTo(
                user.getEmail()
        );


        message.setSubject(
                "Your USP SpaceHub Account"
        );


        String loginUrl =
                baseUrl + "/login";


        String body =
                """
                Hello %s,

                Your USP SpaceHub account has been created.

                Account Details
                ------------------------------
                Student/Staff ID: %s
                Email: %s
                Role: %s
                Department: %s

                Temporary Password:
                %s

                Sign in here:
                %s

                Please change your temporary password after signing in.

                For security, do not share your password with anyone.

                Regards,
                USP SpaceHub
                The University of the South Pacific
                """
                .formatted(
                        user.getName(),
                        user.getUserCode(),
                        user.getEmail(),
                        user.getRole(),
                        user.getDepartment() == null
                                ? "-"
                                : user.getDepartment(),
                        temporaryPassword,
                        loginUrl
                );


        message.setText(
                body
        );


        mailSender.send(
                message
        );
    }



    /* =========================================================
       PASSWORD CHANGED NOTIFICATION
       ========================================================= */

    public void sendPasswordChangedEmail(
            UserAccount user
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setFrom(
                fromAddress
        );


        message.setTo(
                user.getEmail()
        );


        message.setSubject(
                "USP SpaceHub Password Changed"
        );


        String body =
                """
                Hello %s,

                The password for your USP SpaceHub account was changed successfully.

                Account:
                %s

                If you made this change, no further action is required.

                If you did not change your password, please contact the system administrator immediately.

                Regards,
                USP SpaceHub
                The University of the South Pacific
                """
                .formatted(
                        user.getName(),
                        user.getEmail()
                );


        message.setText(
                body
        );


        mailSender.send(
                message
        );
    }



    /* =========================================================
       PASSWORD RESET LINK EMAIL
       We will connect this later.
       ========================================================= */

    public void sendPasswordResetEmail(
            UserAccount user,
            String resetToken
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setFrom(
                fromAddress
        );


        message.setTo(
                user.getEmail()
        );


        message.setSubject(
                "Reset Your USP SpaceHub Password"
        );


        String resetUrl =
                baseUrl
                        +
                "/reset-password?token="
                        +
                resetToken;


        String body =
                """
                Hello %s,

                We received a request to reset your USP SpaceHub password.

                Use the following link to create a new password:

                %s

                This link is temporary and will expire.

                If you did not request a password reset, you can ignore this email.

                Regards,
                USP SpaceHub
                The University of the South Pacific
                """
                .formatted(
                        user.getName(),
                        resetUrl
                );


        message.setText(
                body
        );


        mailSender.send(
                message
        );
    }

}