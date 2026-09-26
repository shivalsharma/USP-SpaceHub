package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository repo;

    public UserAccount require(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Authentication required");
        return repo.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}