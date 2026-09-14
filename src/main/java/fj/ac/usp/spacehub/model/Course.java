package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name="courses") @Getter @Setter @NoArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=30) private String code;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private int courseRoll;
    @Column(nullable=false) private int requiredLabSessions;
    @Column(nullable=false) private int requiredTutorialSessions;
    @Column(nullable=false) private int maxWeeklyBookings = 4;
    @Column(nullable=false) private int primeTimeAllowance = 1;
    @Column(nullable=false) private boolean active = true;
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="course_required_facilities", joinColumns=@JoinColumn(name="course_id"))
    @Column(name="facility_code") private Set<String> requiredFacilities = new LinkedHashSet<>();
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="course_lecturers", joinColumns=@JoinColumn(name="course_id"), inverseJoinColumns=@JoinColumn(name="user_id"))
    private Set<UserAccount> lecturers = new LinkedHashSet<>();
}
