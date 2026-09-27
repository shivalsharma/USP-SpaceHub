package fj.ac.usp.spacehub.dto;

import fj.ac.usp.spacehub.model.BookingType;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.*;

@Data
@NoArgsConstructor
public class SearchForm {
    private Long courseId;

    @NotNull
    private BookingType bookingType;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    /**
     * For LAB/TUTORIAL this value is overwritten on the server with the
     * selected course class roll. For ECA it is entered by the user.
     */
    @Min(1)
    @Max(2000)
    private int expectedStudents = 1;

    /** ECA only. Kept on the search form so it can be forwarded to booking creation. */
    @Size(max = 120)
    private String activityName;

    /** Extra hard requirements selected by the user. Course requirements are added automatically. */
    private String requiredFacilitiesCsv;

    /** Soft preferences only; these never make a room invalid. */
    private String optionalFacilitiesCsv;

    /** Soft preference only. */
    private String preferredBuilding;

    @Min(0)
    @Max(4)
    private int flexibilityHours;

    private boolean flexibleDate;

    public void setPurpose(String ignored) {
        // Legacy method retained for compatibility with older tests/forms.
    }
}
