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

    /*
     * Read users directly from the SpaceHub MySQL users table.
     *
     * Login username = email
     * Password = password_hash
     * Enabled/disabled = active
     */
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {

        JdbcUserDetailsManager manager =
                new JdbcUserDetailsManager(dataSource);

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


    /*
     * Your demo-account passwords are stored as BCrypt hashes.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /*
     * Connect the database UserDetailsService with BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


    /*
     * Main Spring Security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                // Login page + files needed before login
                .requestMatchers(
                    "/login",
                    "/css/**",
                    "/images/**",
                    "/js/**",
                    "/favicon.ico",
                    "/error"
                )
                .permitAll()

                // Administrator pages
                .requestMatchers("/admin/**")
                .hasAnyRole("ADMIN", "IT_ADMIN")

                // Every other page requires login
                .anyRequest()
                .authenticated()
            )

            .formLogin(form -> form

                // Your custom login page
                .loginPage("/login")

                // Login form POST destination
                .loginProcessingUrl("/login")

                // Where users go after successful login
                .defaultSuccessUrl("/dashboard", true)

                // Failed login
                .failureUrl("/login?error")

                .permitAll()
            )

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/login?logout")

                .invalidateHttpSession(true)

                .deleteCookies("JSESSIONID")

                .permitAll()
            );

        return http.build();
    }
}