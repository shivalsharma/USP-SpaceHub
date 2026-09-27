package fj.ac.usp.spacehub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.*;

@Data
public class CourseForm {
    private Long id;
    @NotBlank private String code;
    @NotBlank private String name;
    @Min(1) private int courseRoll;
    @Min(0) private int requiredLabSessions;
    @Min(0) private int requiredTutorialSessions;
    @Min(1) private int maxWeeklyBookings = 4;
    @Min(0) private int primeTimeAllowance = 1;
    private boolean active = true;
    private String requiredFacilitiesCsv;
    private List<Long> lecturerIds = new ArrayList<>();
}