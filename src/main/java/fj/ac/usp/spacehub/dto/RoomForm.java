package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoomForm {
    private Long id;
    @NotBlank private String code;
    @NotBlank private String name;
    @NotBlank private String building;
    @Min(1) private int capacity;
    @Min(0) private int computerCount;
    @Min(0) private int operationalComputers;
    private boolean projectorAvailable;
    private boolean projectorOperational;
    private boolean whiteboardAvailable;
    private boolean whiteboardOperational;
    @NotNull private AccessLevel accessLevel = AccessLevel.BOTH;
    @NotNull private RoomStatus status = RoomStatus.ACTIVE;
    private String departmentRestriction;
    private String courseRestriction;
    private String facilitiesCsv;
    private String faultyFacilitiesCsv;
}