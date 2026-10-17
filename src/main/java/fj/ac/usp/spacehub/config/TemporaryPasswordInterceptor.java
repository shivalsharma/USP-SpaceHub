package fj.ac.usp.spacehub.config;

import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;


@Component
@RequiredArgsConstructor
public class TemporaryPasswordInterceptor
        implements HandlerInterceptor {


    private final UserRepository userRepository;



    /* =========================================================
       CHECK REQUEST BEFORE CONTROLLER EXECUTES
       ========================================================= */

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();



        /* -----------------------------------------------------
           USER IS NOT LOGGED IN
           ----------------------------------------------------- */

        if (
                authentication == null
                ||
                !authentication.isAuthenticated()
                ||
                authentication instanceof AnonymousAuthenticationToken
        ) {

            return true;

        }



        /* -----------------------------------------------------
           FIND CURRENT USER
           ----------------------------------------------------- */

        UserAccount user =
                userRepository
                        .findByEmailIgnoreCase(
                                authentication.getName()
                        )
                        .orElse(null);


        if (user == null) {

            return true;

        }



        /* -----------------------------------------------------
           NORMAL PASSWORD
           ----------------------------------------------------- */

        if (
                !user.isMustChangePassword()
        ) {

            return true;

        }



        /* -----------------------------------------------------
           TEMPORARY PASSWORD USER
           ----------------------------------------------------- */

        String requestPath =
                request.getRequestURI();



        /*
         * Allow the user to reach the Change Password page.
         *
         * This allows both:
         *
         * GET  /profile/change-password
         * POST /profile/change-password
         */
        if (
                requestPath.equals(
                        "/profile/change-password"
                )
        ) {

            return true;

        }



        /*
         * Allow logout so the user is never trapped
         * inside the authenticated session.
         */
        if (
                requestPath.equals(
                        "/logout"
                )
        ) {

            return true;

        }



        /* -----------------------------------------------------
           FORCE PASSWORD CHANGE
           ----------------------------------------------------- */

        response.sendRedirect(
                request.getContextPath()
                        +
                "/profile/change-password"
        );


        return false;
    }

}