package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class ApprovalService {
    private final BookingRepository bookings;
    private final AlternativeSuggestionRepository alternatives;
    private final RoomRepository rooms;
    private final BookingValidationService validation;
    private final ScoringService scoring;
    private final AlternativeService alternativeService;
    private final NotificationService notifications;
    private final AuditService audit;
    private final SettingsService settings;

    public List<ApprovalRecommendation> recommendations(Long bookingId) {
        Booking focus = bookings.findById(bookingId).orElseThrow();
        List<Booking> group = bookings.findRoomOverlaps(focus.getRoom().getId(), focus.getDate(), focus.getStartTime(), focus.getEndTime(), List.of(BookingStatus.PENDING, BookingStatus.ALTERNATIVE_PROPOSED));
        if (group.stream().noneMatch(b -> b.getId().equals(focus.getId()))) group = new ArrayList<>(List.of(focus));
        List<ApprovalRecommendation> list = new ArrayList<>();
        for (Booking b : group) list.add(buildRecommendation(b));
        Comparator<ApprovalRecommendation> cmp = Comparator
                .comparing(ApprovalRecommendation::isValid).reversed()
                .thenComparing(ApprovalRecommendation::getPriorityLevel, Comparator.reverseOrder())
                .thenComparing(ApprovalRecommendation::getCourseNeed, Comparator.reverseOrder())
                .thenComparing(ApprovalRecommendation::getTimeFairness, Comparator.reverseOrder())
                .thenComparing(ApprovalRecommendation::getRoomFairness, Comparator.reverseOrder())
                .thenComparing(ApprovalRecommendation::getSuitability, Comparator.reverseOrder())
                .thenComparing(ApprovalRecommendation::getAlternativeScarcity, Comparator.reverseOrder())
                .thenComparing(a -> a.getBooking().getCreatedAt());
        list.sort(cmp);
        if (list.size() > 1)
            audit.log("CONFLICT_DETECTED", "Booking", focus.getId(), list.size() + " competing requests for " + focus.getRoom().getCode() + " " + focus.getDate() + " " + focus.getStartTime() + "-" + focus.getEndTime());
        if (!list.isEmpty())
            audit.log("APPROVAL_RECOMMENDATION", "Booking", list.getFirst().getBooking().getId(), "Recommended first with display score " + list.getFirst().getDisplayScore() + ". " + list.getFirst().getExplanation());
        return list;
    }

    private ApprovalRecommendation buildRecommendation(Booking b) {
        BookingCandidate c = new BookingCandidate(b.getRequester(), b.getCourse(), b.getRoom(), b.getType(), b.getDate(), b.getStartTime(), b.getEndTime(), b.getExpectedStudents(), b.getRequiredFacilities(), b.getId());
        ValidationResult vr = validation.validate(c, false);
        RoomRecommendation rs = scoring.score(c);
        int pri = b.getType().isAcademic() ? 2 : 1;
        double need = scoring.courseNeed(c), time = scoring.timeFairness(c), roomFair = scoring.roomFairness(c), suit = (rs.getFacilityScore() + rs.getCapacityScore()) / 2.0;
        List<AlternativeOption> alts = alternativeService.findAlternatives(b, 3);
        double scarcity = alts.isEmpty() ? 100 : Math.max(10, 100 - alts.size() * 25.0);
        double display = (pri == 2 ? 30 : 10) + need * .20 + time * .15 + roomFair * .10 + suit * .15 + scarcity * .10;
        display = Math.min(100, Math.round(display * 10.0) / 10.0);
        String alt = alts.isEmpty() ? "No suitable alternative found" : alts.get(0).room().getCode() + " on " + alts.get(0).date() + " " + alts.get(0).startTime() + "-" + alts.get(0).endTime() + " (" + alts.get(0).score() + "%)";
        String exp = vr.valid() ? (b.getType().isAcademic() ? "Academic priority (LAB/TUTORIAL). " : "Lower ECA priority. ") +
                "Course need " + Math.round(need) + "%, time fairness " + Math.round(time) + "%, room fairness " + Math.round(roomFair) + "%, suitability " + Math.round(suit) + "%. " +
                (alts.isEmpty() ? "No workable alternative increases this request's scarcity." : "A workable alternative exists.")
                : "Invalid due to hard constraints: " + String.join(" ", vr.violations());
        return ApprovalRecommendation.builder()
                .booking(b).valid(vr.valid()).priorityLevel(pri)
                .courseNeed(need).timeFairness(time).roomFairness(roomFair).suitability(suit)
                .alternativeScarcity(scarcity).displayScore(display).explanation(exp).suggestedAlternative(alt).build();
    }

    @Transactional
    public void approve(Long id, String reason) {
        Booking b = bookings.findById(id).orElseThrow();
        rooms.findByIdForUpdate(b.getRoom().getId()).orElseThrow();
        BookingCandidate c = new BookingCandidate(b.getRequester(), b.getCourse(), b.getRoom(), b.getType(), b.getDate(), b.getStartTime(), b.getEndTime(), b.getExpectedStudents(), b.getRequiredFacilities(), b.getId());
        ValidationResult vr = validation.validate(c, false);
        if (!vr.valid()) throw new IllegalArgumentException("Cannot approve: " + String.join(" ", vr.violations()));
        List<Booking> competitors = bookings.findRoomOverlaps(b.getRoom().getId(), b.getDate(), b.getStartTime(), b.getEndTime(), List.of(BookingStatus.PENDING, BookingStatus.ALTERNATIVE_PROPOSED));
        b.setStatus(BookingStatus.APPROVED);
        b.setAdminDecisionReason(reason == null || reason.isBlank() ? "Approved by administrator" : reason);
        bookings.save(b);
        notifications.notify(b.getRequester(), "Booking approved", summary(b), "APPROVED-" + b.getId());
        audit.log("BOOKING_APPROVED", "Booking", b.getId(), b.getAdminDecisionReason());
        processCompetitors(b, competitors);
    }

    @Transactional
    public void reject(Long id, String reason) {
        Booking b = bookings.findById(id).orElseThrow();
        b.setStatus(BookingStatus.REJECTED);
        b.setAdminDecisionReason(reason == null || reason.isBlank() ? "Rejected by administrator" : reason);
        bookings.save(b);
        notifications.notify(b.getRequester(), "Booking rejected", b.getAdminDecisionReason(), "REJECTED-" + b.getId());
        audit.log("BOOKING_REJECTED", "Booking", b.getId(), b.getAdminDecisionReason());
    }

    @Transactional
    public void approveWithOverride(Long id, String reason) {
        if (!settings.getBoolean("admin.override.enabled", true))
            throw new IllegalArgumentException("Admin override is disabled in settings.");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("An override reason is required.");
        Booking b = bookings.findById(id).orElseThrow();
        rooms.findByIdForUpdate(b.getRoom().getId()).orElseThrow();
        BookingCandidate c = new BookingCandidate(b.getRequester(), b.getCourse(), b.getRoom(), b.getType(), b.getDate(), b.getStartTime(), b.getEndTime(), b.getExpectedStudents(), b.getRequiredFacilities(), b.getId());
        List<Booking> competitors = bookings.findRoomOverlaps(b.getRoom().getId(), b.getDate(), b.getStartTime(), b.getEndTime(), List.of(BookingStatus.PENDING, BookingStatus.ALTERNATIVE_PROPOSED));
        ValidationResult vr = validation.validate(c, false);
        List<String> nonOverride = vr.violations().stream()
                .filter(x -> !(x.contains("weekly booking allowance") || x.contains("daily booking allowance") || x.contains("weekly booking limit") || x.contains("prime-time allowance")))
                .toList();
        if (!nonOverride.isEmpty())
            throw new IllegalArgumentException("Override not allowed for safety/access/conflict rule(s): " + String.join(" ", nonOverride));
        b.setOverrideReason(reason);
        b.setStatus(BookingStatus.APPROVED);
        b.setAdminDecisionReason("Approved with administrative override");
        bookings.save(b);
        notifications.notify(b.getRequester(), "Booking approved with override", summary(b), "OVERRIDE-" + b.getId());
        audit.log("ADMIN_OVERRIDE", "Booking", b.getId(), reason);
        processCompetitors(b, competitors);
    }

    private void processCompetitors(Booking approved, List<Booking> competitors) {
        for (Booking other : competitors) {
            if (other.getId().equals(approved.getId()) || other.getStatus() == BookingStatus.APPROVED) continue;
            List<AlternativeOption> opts = alternativeService.findAlternatives(other, 1);
            if (opts.isEmpty()) {
                other.setStatus(BookingStatus.REJECTED);
                other.setAdminDecisionReason("Competing request approved and no suitable alternative was available.");
                bookings.save(other);
                notifications.notify(other.getRequester(), "Booking request unsuccessful", other.getAdminDecisionReason(), "CONFLICT-REJECT-" + other.getId());
            } else {
                AlternativeOption o = opts.get(0);
                AlternativeSuggestion suggestion = new AlternativeSuggestion();
                suggestion.setBooking(other);
                suggestion.setRoom(o.room());
                suggestion.setDate(o.date());
                suggestion.setStartTime(o.startTime());
                suggestion.setEndTime(o.endTime());
                suggestion.setScore(o.score());
                suggestion.setExplanation(o.explanation());
                alternatives.save(suggestion);
                other.setStatus(BookingStatus.ALTERNATIVE_PROPOSED);
                other.setAdminDecisionReason("Competing request approved; alternative proposed.");
                bookings.save(other);
                notifications.notify(other.getRequester(), "Alternative booking suggested",
                        "Original room/time was allocated to another request. Suggested: " + o.room().getCode() + " on " + o.date() + " " + o.startTime() + "-" + o.endTime() + ".",
                        "ALTERNATIVE-" + other.getId() + "-" + suggestion.getId());
                audit.log("ALTERNATIVE_SUGGESTED", "Booking", other.getId(), o.explanation());
            }
        }
    }

    private String summary(Booking b) {
        return b.getType() + " booking in " + b.getRoom().getCode() + " on " + b.getDate() + " from " + b.getStartTime() + " to " + b.getEndTime() + ".";
    }
}