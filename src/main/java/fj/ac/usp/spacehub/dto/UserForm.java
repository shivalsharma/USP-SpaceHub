package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserForm {
    private Long id;
    @NotBlank private String userCode;
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotNull private UserRole role;
    private boolean active = true;
    private String department;
    private Integer maxDailyBookings;
    private Integer maxWeeklyBookings;
    private String password;
}