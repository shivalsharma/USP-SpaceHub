package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.Room;
import lombok.*;
import java.util.*;

@Data @Builder
public class RoomRecommendation {
    private Room room;
    private double score;
    private double facilityScore;
    private double capacityScore;
    private double timeFairnessScore;
    private double courseNeedScore;
    private double utilizationScore;
    private double roomFairnessScore;
    private long pendingCompetition;
    private List<String> reasons;
}