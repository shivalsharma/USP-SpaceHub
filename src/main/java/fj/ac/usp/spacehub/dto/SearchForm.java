package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.BookingType;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.*;

@Data @NoArgsConstructor
public class SearchForm {
    private Long courseId;
    @NotNull private BookingType bookingType;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate date;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) private LocalTime startTime;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) private LocalTime endTime;
    @Min(1) private int expectedStudents = 1;
    private String requiredFacilitiesCsv;
    private String optionalFacilitiesCsv;
    private String preferredBuilding;
    @Min(0) @Max(4) private int flexibilityHours;
    private boolean flexibleDate;

    public void setPurpose(String testLab) {} // Legacy method kept for compatibility
}