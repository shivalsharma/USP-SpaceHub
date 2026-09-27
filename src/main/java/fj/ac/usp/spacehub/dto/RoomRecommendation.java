package fj.ac.usp.spacehub.dto;

import fj.ac.usp.spacehub.model.Room;
import lombok.*;

import java.util.*;

@Data
@Builder
public class RoomRecommendation {
    private Room room;

    /** Overall room suitability only. Request-priority factors do not inflate this score. */
    private double score;

    // Room-suitability factors
    private double facilityScore;
    private double capacityScore;
    private double scheduleFitScore;
    private double utilizationScore;
    private double roomFairnessScore;

    // Request-level context, useful for explanation/admin conflict comparison
    private double timeFairnessScore;
    private double courseNeedScore;
    private double alternativeScarcityScore;
    private double requestPriorityScore;

    private long pendingCompetition;
    private List<String> reasons;
}
