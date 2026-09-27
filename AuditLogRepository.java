package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{
    List<AuditLog> findTop250ByOrderByCreatedAtDesc();
}
