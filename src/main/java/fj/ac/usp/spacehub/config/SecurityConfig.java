package fj.ac.usp.spacehub.config;
import fj.ac.usp.spacehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration @RequiredArgsConstructor
public class SecurityConfig {
    private final UserRepository users;
    @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(){
        return username -> users.findByEmailIgnoreCase(username).map(u->User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles(u.getRole().name()).disabled(!u.isActive()).build()).orElseThrow(()->new UsernameNotFoundException("User not found"));
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests(a->a
            .requestMatchers("/css/**","/login","/error").permitAll()
            .requestMatchers("/h2-console/**").hasAnyRole("ADMIN","IT_ADMIN")
            .requestMatchers("/admin/users/**","/admin/rooms/**").hasAnyRole("ADMIN","IT_ADMIN")
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
            .formLogin(f->f.loginPage("/login").defaultSuccessUrl("/dashboard",true).permitAll())
            .logout(l->l.logoutSuccessUrl("/login?logout"))
            .csrf(c->c.ignoringRequestMatchers("/h2-console/**"))
            .headers(h->h.frameOptions(f->f.sameOrigin()));
        return http.build();
    }
}
