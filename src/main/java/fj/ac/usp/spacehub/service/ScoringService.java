package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor
public class ScoringService {
    private final BookingRepository bookings;
    private final SettingsService settings;
    private final BookingValidationService validation;
    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.PENDING, BookingStatus.APPROVED, BookingStatus.ALTERNATIVE_PROPOSED);

    public RoomRecommendation score(BookingCandidate c) {
        double facility = facilityScore(c);
        double capacity = clamp(100.0 * c.expectedStudents() / Math.max(1, c.room().getCapacity()));
        double time = timeFairness(c);
        double need = courseNeed(c);
        double utilization = roomUtilization(c.room(), c.date());
        double roomFair = roomFairness(c);
        double wF = settings.getDouble("score.weight.facility", 25);
        double wC = settings.getDouble("score.weight.capacity", 20);
        double wT = settings.getDouble("score.weight.timeFairness", 20);
        double wN = settings.getDouble("score.weight.courseNeed", 15);
        double wU = settings.getDouble("score.weight.utilization", 10);
        double wR = settings.getDouble("score.weight.roomFairness", 10);
        double total = wF + wC + wT + wN + wU + wR;
        if (total <= 0) total = 100;
        double score = (facility * wF + capacity * wC + time * wT + need * wN + utilization * wU + roomFair * wR) / total;
        List<String> reasons = new ArrayList<>();
        if (capacity >= 85) reasons.add("Excellent capacity fit (" + c.expectedStudents() + " / " + c.room().getCapacity() + ").");
        else reasons.add("Capacity is valid with " + Math.round(capacity) + "% seat utilization.");
        if (facility >= 95) reasons.add("Required facilities are an exact or near-exact operational match.");
        if (time >= 75) reasons.add("Prime-time allocation is fair based on the requester's recent usage.");
        if (need >= 60 && c.course() != null) reasons.add(c.course().getCode() + " still has significant remaining weekly booking need.");
        if (utilization >= 60) reasons.add("This room is not heavily utilized this week.");
        if (roomFair >= 70) reasons.add("The requester has not overused this room recently.");
        long pending = validation.pendingCompetition(c.room(), c.date(), c.startTime(), c.endTime());
        if (pending > 0) reasons.add(pending + " competing pending request(s) exist for this room/time; admin conflict review will apply.");
        return RoomRecommendation.builder()
                .room(c.room()).score(round(score)).facilityScore(round(facility))
                .capacityScore(round(capacity)).timeFairnessScore(round(time))
                .courseNeedScore(round(need)).utilizationScore(round(utilization))
                .roomFairnessScore(round(roomFair)).pendingCompetition(pending).reasons(reasons).build();
    }

    public double facilityScore(BookingCandidate c) {
        Set<String> req = validation.combinedRequirements(c.course(), c.requiredFacilities());
        Set<String> room = FacilityUtil.allFacilities(c.room());
        int extras = 0;
        for (String f : room) if (!req.contains(f) && !Set.of("PROJECTOR", "WHITEBOARD").contains(f)) extras++;
        double score = 100 - extras * 5.0;
        if (req.contains("COMPUTER") && c.room().getOperationalComputers() > c.expectedStudents() * 1.75) score -= 5;
        return clamp(score);
    }

    public double timeFairness(BookingCandidate c) {
        if (!validation.isPrime(c.startTime(), c.endTime())) return 100;
        LocalDate ws = BookingValidationService.weekStart(c.date()), we = ws.plusDays(6);
        long used = bookings.findByRequesterIdAndDateBetweenAndStatusIn(c.requester().getId(), ws, we, ACTIVE).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId()))
                .filter(b -> validation.isPrime(b.getStartTime(), b.getEndTime())).count();
        return clamp(100 - used * 25.0);
    }

    public double courseNeed(BookingCandidate c) {
        if (c.course() == null || !c.type().isAcademic()) return 50;
        LocalDate ws = BookingValidationService.weekStart(c.date()), we = ws.plusDays(6);
        int configuredNeed = c.type() == BookingType.LAB ? c.course().getRequiredLabSessions() : c.course().getRequiredTutorialSessions();
        int target = Math.max(1, configuredNeed > 0 ? configuredNeed : c.course().getMaxWeeklyBookings());
        long used = bookings.findByCourseIdAndDateBetweenAndStatusIn(c.course().getId(), ws, we, ACTIVE).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId()))
                .filter(b -> b.getType() == c.type()).count();
        return clamp(100.0 * Math.max(0, target - used) / target);
    }

    public double roomFairness(BookingCandidate c) {
        LocalDate ws = BookingValidationService.weekStart(c.date()), we = ws.plusDays(6);
        long used = bookings.findByRequesterIdAndDateBetweenAndStatusIn(c.requester().getId(), ws, we, List.of(BookingStatus.APPROVED)).stream()
                .filter(b -> b.getRoom().getId().equals(c.room().getId())).count();
        return clamp(100.0 / (1 + 0.5 * used));
    }

    public double roomUtilization(Room room, LocalDate date) {
        LocalDate ws = BookingValidationService.weekStart(date), we = ws.plusDays(6);
        double hours = 0;
        for (Booking b : bookings.findByRoomIdAndDateBetweenAndStatus(room.getId(), ws, we, BookingStatus.APPROVED))
            hours += ChronoUnit.MINUTES.between(b.getStartTime(), b.getEndTime()) / 60.0;
        double available = 7.0 * ChronoUnit.MINUTES.between(
                settings.getTime("booking.day.start", LocalTime.of(8, 0)),
                settings.getTime("booking.day.end", LocalTime.of(22, 0))) / 60.0;
        return clamp(100 - (hours / Math.max(1, available)) * 100);
    }

    private static double clamp(double v) { return Math.max(0, Math.min(100, v)); }
    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
}