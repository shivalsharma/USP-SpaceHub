package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repo;

    public void log(String action, String entityType, Object entityId, String details) {
        AuditLog a = new AuditLog();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        a.setActorEmail(auth != null && auth.isAuthenticated() ? auth.getName() : "SYSTEM");
        a.setAction(action);
        a.setEntityType(entityType);
        a.setEntityId(entityId == null ? null : String.valueOf(entityId));
        a.setDetails(details);
        repo.save(a);
    }
}