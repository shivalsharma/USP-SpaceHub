package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity @Table(name="bookings") @Getter @Setter @NoArgsConstructor
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private UserAccount requester;
    @ManyToOne(fetch=FetchType.EAGER) private Course course;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private Room room;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private BookingType type;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private BookingStatus status = BookingStatus.PENDING;
    @Column(nullable=false) private LocalDate date;
    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;
    @Column(nullable=false) private int expectedStudents;
    @Column(nullable=false, length=1000) private String purpose;
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="booking_required_facilities", joinColumns=@JoinColumn(name="booking_id"))
    @Column(name="facility_code") private Set<String> requiredFacilities = new LinkedHashSet<>();
    private Double searchScore;
    @Column(length=2000) private String recommendationReason;
    @Column(length=2000) private String adminDecisionReason;
    @Column(length=2000) private String overrideReason;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate void touch(){ updatedAt = LocalDateTime.now(); }
}
