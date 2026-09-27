package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AlternativeSuggestionRepository extends JpaRepository<AlternativeSuggestion, Long> {
    List<AlternativeSuggestion> findByBookingIdOrderByScoreDesc(Long bookingId);
    List<AlternativeSuggestion> findByBookingRequesterIdAndStatusOrderByCreatedAtDesc(Long userId, AlternativeStatus status);
}