package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="alternative_suggestions") @Getter @Setter @NoArgsConstructor
public class AlternativeSuggestion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private Booking booking;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private Room room;
    @Column(nullable=false) private LocalDate date;
    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;
    @Column(nullable=false) private double score;
    @Column(nullable=false, length=1000) private String explanation;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AlternativeStatus status = AlternativeStatus.PROPOSED;
    @Column(nullable=false) private boolean preserveOriginalOnReject = false;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
