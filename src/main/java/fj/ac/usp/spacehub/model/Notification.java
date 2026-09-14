package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="notifications", uniqueConstraints=@UniqueConstraint(name="uk_notification_dedup", columnNames={"dedup_key"}))
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private UserAccount user;
    @Column(nullable=false) private String title;
    @Column(nullable=false, length=2000) private String message;
    @Column(nullable=false) private boolean readFlag = false;
    @Column(name="dedup_key", length=180) private String dedupKey;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
