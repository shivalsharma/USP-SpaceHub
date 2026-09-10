package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.Room;
import lombok.Builder;
import lombok.Data;
import java.time.*;
import java.util.List;
@Data @Builder
public class SearchAlternative {
    private Room room;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double score;
    private List<String> reasons;
}
