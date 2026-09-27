package fj.ac.usp.spacehub.service;

import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScoringService {
    private final BookingRepository bookings;
    private final SettingsService settings;
    private final BookingValidationService validation;

    /**
     * Room suitability is intentionally separated from request priority.
     * Course need, prime-time fairness and scarcity are request-level factors and
     * therefore do not decide which room is best within one search.
     */
    public RoomRecommendation score(BookingCandidate c) {
        double facility = facilityScore(c);
        double capacity = capacityScore(c);
        double schedule = scheduleFit(c);
        double utilization = roomUtilization(c.room(), c.date());
        double roomFair = roomFairness(c);

        double wF = settings.getDouble("score.weight.facility", 30);
        double wC = settings.getDouble("score.weight.capacity", 25);
        double wS = settings.getDouble("score.weight.scheduleFit", 15);
        double wU = settings.getDouble("score.weight.utilization", 15);
        double wR = settings.getDouble("score.weight.roomFairness", 15);
        double total = wF + wC + wS + wU + wR;
        if (total <= 0) total = 100;

        double roomScore = (facility * wF + capacity * wC + schedule * wS + utilization * wU + roomFair * wR) / total;
        double time = timeFairness(c);
        double need = courseNeed(c);

        List<String> reasons = new ArrayList<>();
        if (capacity >= 90)
            reasons.add("Excellent capacity fit: " + c.expectedStudents() + " people for a room of " + c.room().getCapacity() + ".");
        else if (capacity >= 70)
            reasons.add("Good capacity fit without excessive unused space.");
        else
            reasons.add("Capacity is sufficient, although a smaller valid room would use space more efficiently.");

        if (facility >= 95)
            reasons.add("Required facilities are available and the room avoids unnecessary specialized-resource use.");
        else if (facility >= 75)
            reasons.add("All required facilities are available; some additional specialized resources would be unused.");

        if (schedule >= 85)
            reasons.add("The slot fits well around this room's existing timetable and avoids creating awkward gaps.");

        if (utilization >= 70)
            reasons.add("This room is currently less heavily used, helping balance room utilization.");

        if (roomFair >= 75)
            reasons.add("The requester has not repeatedly used this room recently.");

        long pending = validation.pendingCompetition(c.room(), c.date(), c.startTime(), c.endTime());
        if (pending > 0)
            reasons.add(pending + " competing pending request(s) exist for this room/time; admin conflict review will apply.");

        return RoomRecommendation.builder()
                .room(c.room())
                .score(round(roomScore))
                .facilityScore(round(facility))
                .capacityScore(round(capacity))
                .scheduleFitScore(round(schedule))
                .utilizationScore(round(utilization))
                .roomFairnessScore(round(roomFair))
                .timeFairnessScore(round(time))
                .courseNeedScore(round(need))
                .alternativeScarcityScore(0)
                .requestPriorityScore(0)
                .pendingCompetition(pending)
                .reasons(reasons)
                .build();
    }

    /** Adds request-level context after SearchService knows how many valid rooms exist. */
    public void applyRequestContext(RoomRecommendation rec, BookingType type, int validRoomCount) {
        double scarcity = alternativeScarcity(validRoomCount);
        double priority;

        if (type != null && type.isAcademic()) {
            double wTime = settings.getDouble("priority.weight.timeFairness", 30);
            double wNeed = settings.getDouble("priority.weight.courseNeed", 45);
            double wScarcity = settings.getDouble("priority.weight.scarcity", 25);
            double total = Math.max(1, wTime + wNeed + wScarcity);
            priority = (rec.getTimeFairnessScore() * wTime + rec.getCourseNeedScore() * wNeed + scarcity * wScarcity) / total;
        } else {
            double wTime = settings.getDouble("priority.weight.timeFairness", 55);
            double wScarcity = settings.getDouble("priority.weight.scarcity", 45);
            double total = Math.max(1, wTime + wScarcity);
            priority = (rec.getTimeFairnessScore() * wTime + scarcity * wScarcity) / total;
        }

        rec.setAlternativeScarcityScore(round(scarcity));
        rec.setRequestPriorityScore(round(priority));

        if (scarcity >= 85)
            rec.getReasons().add("Very few rooms can satisfy this request, so suitable alternatives are scarce.");
        if (type != null && type.isAcademic() && rec.getCourseNeedScore() >= 70)
            rec.getReasons().add("The course still has a high remaining session need for this booking type.");
        if (rec.getTimeFairnessScore() >= 75)
            rec.getReasons().add("The requested time is fair based on the requester's recent prime-time allocation.");
    }

    public double facilityScore(BookingCandidate c) {
        Set<String> req = validation.combinedRequirements(c.course(), c.requiredFacilities());
        Set<String> room = FacilityUtil.allFacilities(c.room());

        int specializedExtras = 0;
        for (String f : room) {
            if (!req.contains(f) && !Set.of("PROJECTOR", "WHITEBOARD").contains(f))
                specializedExtras++;
        }

        // Preserve specialized rooms/resources when a simpler room can satisfy the request.
        double score = 100 - specializedExtras * 7.0;

        if (req.contains("COMPUTER") && c.expectedStudents() > 0 &&
                c.room().getOperationalComputers() > c.expectedStudents() * 1.75)
            score -= 7;

        return clamp(score);
    }

    public double capacityScore(BookingCandidate c) {
        return clamp(100.0 * c.expectedStudents() / Math.max(1, c.room().getCapacity()));
    }

    public double timeFairness(BookingCandidate c) {
        if (!validation.isPrime(c.startTime(), c.endTime())) return 100;

        LocalDate ws = BookingValidationService.weekStart(c.date());
        LocalDate we = ws.plusDays(6);
        long used = bookings.findByRequesterIdAndDateBetweenAndStatusIn(
                        c.requester().getId(), ws, we, List.of(BookingStatus.APPROVED)).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId()))
                .filter(b -> validation.isPrime(b.getStartTime(), b.getEndTime()))
                .count();

        double penalty = settings.getDouble("fairness.prime.penalty", 25);
        return clamp(100 - used * penalty);
    }

    public double courseNeed(BookingCandidate c) {
        if (c.course() == null || !c.type().isAcademic()) return 50;

        LocalDate ws = BookingValidationService.weekStart(c.date());
        LocalDate we = ws.plusDays(6);
        int configuredNeed = c.type() == BookingType.LAB
                ? c.course().getRequiredLabSessions()
                : c.course().getRequiredTutorialSessions();
        int target = Math.max(1, configuredNeed > 0 ? configuredNeed : c.course().getMaxWeeklyBookings());

        long approved = bookings.findByCourseIdAndDateBetweenAndStatusIn(
                        c.course().getId(), ws, we, List.of(BookingStatus.APPROVED)).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId()))
                .filter(b -> b.getType() == c.type())
                .count();

        return clamp(100.0 * Math.max(0, target - approved) / target);
    }

    public double roomFairness(BookingCandidate c) {
        LocalDate ws = BookingValidationService.weekStart(c.date());
        LocalDate we = ws.plusDays(6);
        long used = bookings.findByRequesterIdAndDateBetweenAndStatusIn(
                        c.requester().getId(), ws, we, List.of(BookingStatus.APPROVED)).stream()
                .filter(b -> b.getRoom() != null && b.getRoom().getId().equals(c.room().getId()))
                .count();

        return clamp(100.0 / (1 + 0.5 * used));
    }

    /** Higher score means the room is currently under-used and can absorb more demand. */
    public double roomUtilization(Room room, LocalDate date) {
        LocalDate ws = BookingValidationService.weekStart(date);
        LocalDate we = ws.plusDays(6);
        double hours = 0;

        for (Booking b : bookings.findByRoomIdAndDateBetweenAndStatus(room.getId(), ws, we, BookingStatus.APPROVED))
            hours += ChronoUnit.MINUTES.between(b.getStartTime(), b.getEndTime()) / 60.0;

        double available = 7.0 * ChronoUnit.MINUTES.between(
                settings.getTime("booking.day.start", LocalTime.of(8, 0)),
                settings.getTime("booking.day.end", LocalTime.of(22, 0))) / 60.0;

        return clamp(100 - (hours / Math.max(1, available)) * 100);
    }

    /**
     * Rewards a proposed slot that sits neatly next to existing approved sessions.
     * It is a soft factor only; a completely empty room is still a valid choice.
     */
    public double scheduleFit(BookingCandidate c) {
        List<Booking> day = bookings.findByRoomIdAndDateBetweenAndStatus(
                c.room().getId(), c.date(), c.date(), BookingStatus.APPROVED);
        if (day.isEmpty()) return 70;

        long beforeGap = Long.MAX_VALUE;
        long afterGap = Long.MAX_VALUE;

        for (Booking b : day) {
            if (!b.getEndTime().isAfter(c.startTime()))
                beforeGap = Math.min(beforeGap, ChronoUnit.MINUTES.between(b.getEndTime(), c.startTime()));
            if (!b.getStartTime().isBefore(c.endTime()))
                afterGap = Math.min(afterGap, ChronoUnit.MINUTES.between(c.endTime(), b.getStartTime()));
        }

        double score = 60;
        score += adjacencyBonus(beforeGap);
        score += adjacencyBonus(afterGap);
        return clamp(score);
    }

    private double adjacencyBonus(long gapMinutes) {
        if (gapMinutes == Long.MAX_VALUE) return 5;
        if (gapMinutes == 0) return 20;
        if (gapMinutes <= 30) return 17;
        if (gapMinutes <= 60) return 12;
        if (gapMinutes <= 120) return 7;
        return 3;
    }

    public double alternativeScarcity(int validRoomCount) {
        if (validRoomCount <= 1) return 100;
        if (validRoomCount == 2) return 88;
        if (validRoomCount <= 4) return 72;
        if (validRoomCount <= 6) return 58;
        return 40;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }

    private static double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
