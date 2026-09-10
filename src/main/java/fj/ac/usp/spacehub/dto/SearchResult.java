package fj.ac.usp.spacehub.dto;
import lombok.*;
import java.util.*;

@Data @Builder
public class SearchResult {
    private List<RoomRecommendation> recommendations;
    private List<SearchAlternative> alternatives;
    private List<String> globalViolations;
    private Map<Long, List<String>> excludedRoomReasons;
}