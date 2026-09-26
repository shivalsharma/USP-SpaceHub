package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="maintenance_windows") @Getter @Setter @NoArgsConstructor
public class MaintenanceWindow {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Room room;
    @Column(nullable=false) private LocalDate date;
    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;
    @Column(nullable=false) private String reason;
}
