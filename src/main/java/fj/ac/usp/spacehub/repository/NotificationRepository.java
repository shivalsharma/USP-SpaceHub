package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificationRepository extends JpaRepository<Notification,Long>{
    List<Notification> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFlagFalse(Long userId);
    Optional<Notification> findByDedupKey(String key);
}
