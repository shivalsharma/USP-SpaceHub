package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="schedule_entries") @Getter @Setter @NoArgsConstructor
public class ScheduleEntry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private UserAccount user;
    @Column(nullable=false) private LocalDate date;
    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;
    @Column(nullable=false) private String title;
}
