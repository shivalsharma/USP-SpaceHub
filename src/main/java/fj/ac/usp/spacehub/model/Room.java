package fj.ac.usp.spacehub.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name="rooms") @Getter @Setter @NoArgsConstructor
public class Room {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=30) private String code;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String building;
    @Column(nullable=false) private int capacity;
    @Column(nullable=false) private int computerCount;
    @Column(nullable=false) private int operationalComputers;
    @Column(nullable=false) private boolean projectorAvailable;
    @Column(nullable=false) private boolean projectorOperational;
    @Column(nullable=false) private boolean whiteboardAvailable;
    @Column(nullable=false) private boolean whiteboardOperational;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AccessLevel accessLevel = AccessLevel.BOTH;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private RoomStatus status = RoomStatus.ACTIVE;
    private String departmentRestriction;
    private String courseRestriction;
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="room_facilities", joinColumns=@JoinColumn(name="room_id"))
    @Column(name="facility_code") private Set<String> facilities = new LinkedHashSet<>();
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="room_faulty_facilities", joinColumns=@JoinColumn(name="room_id"))
    @Column(name="facility_code") private Set<String> faultyFacilities = new LinkedHashSet<>();
}
