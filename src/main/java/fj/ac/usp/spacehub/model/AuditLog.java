package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="audit_logs") @Getter @Setter @NoArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String actorEmail;
    @Column(nullable=false) private String action;
    @Column(nullable=false) private String entityType;
    private String entityId;
    @Column(nullable=false, length=4000) private String details;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
