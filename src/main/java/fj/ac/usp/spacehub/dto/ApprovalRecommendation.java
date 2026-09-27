package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.Booking;
import lombok.*;

@Data @Builder
public class ApprovalRecommendation {
    private Booking booking;
    private boolean valid;
    private int priorityLevel;
    private double courseNeed;
    private double timeFairness;
    private double roomFairness;
    private double suitability;
    private double alternativeScarcity;
    private double displayScore;
    private String explanation;
    private String suggestedAlternative;
}