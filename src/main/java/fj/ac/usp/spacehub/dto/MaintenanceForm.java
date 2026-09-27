package fj.ac.usp.spacehub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.*;

@Data
public class MaintenanceForm {
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate date;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) private LocalTime startTime;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) private LocalTime endTime;
    @NotBlank private String reason;
}