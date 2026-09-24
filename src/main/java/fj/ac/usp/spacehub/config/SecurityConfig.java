package fj.ac.usp.spacehub.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.JdbcUserDetailsManager;

import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {


    /* =========================================================
       USER DETAILS SERVICE
       =========================================================
       Reads login information directly from the MySQL users table.

       Username = email
       Password = password_hash
       Enabled  = active
       ========================================================= */
    @Bean
    public UserDetailsService userDetailsService(
            DataSource dataSource) {


        JdbcUserDetailsManager manager =
                new JdbcUserDetailsManager(dataSource);


        /*
         * Find the user's login details.
         */
        manager.setUsersByUsernameQuery(
                """
                SELECT
                    email AS username,
                    password_hash AS password,
                    active AS enabled
                FROM users
                WHERE email = ?
                """
        );


        /*
         * Find the user's role.
         *
         * Example:
         *
         * ADMIN      -> ROLE_ADMIN
         * IT_ADMIN   -> ROLE_IT_ADMIN
         * LECTURER   -> ROLE_LECTURER
         * STUDENT    -> ROLE_STUDENT
         */
        manager.setAuthoritiesByUsernameQuery(
                """
                SELECT
                    email AS username,
                    CONCAT('ROLE_', role) AS authority
                FROM users
                WHERE email = ?
                """
        );


        return manager;
    }



    /* =========================================================
       PASSWORD ENCODER
       ========================================================= */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }



    /* =========================================================
       AUTHENTICATION PROVIDER
       ========================================================= */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {


        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );


        provider.setPasswordEncoder(
                passwordEncoder
        );


        return provider;
    }



    /* =========================================================
       SECURITY FILTER CHAIN / RBAC
       ========================================================= */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {


        http

                /*
                 * Use our database authentication provider.
                 */
                .authenticationProvider(
                        authenticationProvider
                )


                /* =================================================
                   ROLE-BASED ACCESS CONTROL
                   ================================================= */
                .authorizeHttpRequests(auth -> auth


                        /* =========================================
                           PUBLIC RESOURCES
                           =========================================
                           These can be accessed without logging in.
                           ========================================= */
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/favicon.ico",
                                "/error"
                        )
                        .permitAll()



                        /* =========================================
                           USER MANAGEMENT
                           =========================================
                           ADMIN     = allowed
                           IT_ADMIN  = allowed
                           LECTURER  = blocked
                           STUDENT   = blocked
                           ========================================= */
                        .requestMatchers(
                                "/admin/users",
                                "/admin/users/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "IT_ADMIN"
                        )



                        /* =========================================
                           ROOM ADMINISTRATION
                           =========================================
                           ADMIN     = allowed
                           IT_ADMIN  = allowed
                           LECTURER  = blocked
                           STUDENT   = blocked
                           ========================================= */
                        .requestMatchers(
                                "/admin/rooms",
                                "/admin/rooms/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "IT_ADMIN"
                        )



                        /* =========================================
                           BOOKING APPROVALS
                           =========================================
                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin/approvals",
                                "/admin/approvals/**"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           ADMIN BOOKING MANAGEMENT
                           =========================================
                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin/bookings",
                                "/admin/bookings/**"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           COURSE MANAGEMENT
                           =========================================
                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin/courses",
                                "/admin/courses/**"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           REPORTS
                           =========================================
                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin/reports",
                                "/admin/reports/**"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           AUDIT LOG
                           =========================================
                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin/audit"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           SYSTEM SETTINGS
                           =========================================
                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin/settings",
                                "/admin/settings/**"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           REMAINING ADMIN URLS
                           =========================================
                           This includes /admin itself.

                           ADMIN only.
                           ========================================= */
                        .requestMatchers(
                                "/admin",
                                "/admin/**"
                        )
                        .hasRole(
                                "ADMIN"
                        )



                        /* =========================================
                           NORMAL AUTHENTICATED PAGES
                           =========================================
                           Dashboard
                           Search
                           My Bookings
                           Calendar
                           Notifications
                           Profile

                           All logged-in roles can access these.
                           ========================================= */
                        .anyRequest()
                        .authenticated()
                )



                /* =================================================
                   LOGIN
                   ================================================= */
                .formLogin(form -> form

                        /*
                         * Custom SpaceHub login page.
                         */
                        .loginPage(
                                "/login"
                        )


                        /*
                         * Spring Security processes the form here.
                         */
                        .loginProcessingUrl(
                                "/login"
                        )


                        /*
                         * All users go to the common dashboard
                         * after successful login.
                         *
                         * Later we will make the dashboard
                         * role-aware.
                         */
                        .defaultSuccessUrl(
                                "/dashboard",
                                true
                        )


                        /*
                         * Incorrect username/password.
                         */
                        .failureUrl(
                                "/login?error"
                        )


                        .permitAll()
                )



                /* =================================================
                   LOGOUT
                   ================================================= */
                .logout(logout -> logout

                        .logoutUrl(
                                "/logout"
                        )


                        .logoutSuccessUrl(
                                "/login?logout"
                        )


                        /*
                         * Destroy the logged-in session.
                         */
                        .invalidateHttpSession(
                                true
                        )


                        /*
                         * Remove session cookie.
                         */
                        .deleteCookies(
                                "JSESSIONID"
                        )


                        .permitAll()
                );


        return http.build();
    }

}