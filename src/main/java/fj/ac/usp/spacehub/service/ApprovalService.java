package fj.ac.usp.spacehub.service;

import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
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


    @Transactional(readOnly = true)
    public List<ApprovalRecommendation> recommendations(Long bookingId) {

        Booking focus = bookings.findById(bookingId).orElseThrow();

        List<Booking> group = bookings.findRoomOverlaps(
                focus.getRoom().getId(),
                focus.getDate(),
                focus.getStartTime(),
                focus.getEndTime(),
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.ALTERNATIVE_PROPOSED
                )
        );

        if (group.stream().noneMatch(
                b -> b.getId().equals(focus.getId()))) {

            group = new ArrayList<>(List.of(focus));
        }

        List<ApprovalRecommendation> list = new ArrayList<>();
        boolean conflictGroup = group.size() > 1;

        for (Booking b : group) {
            list.add(buildRecommendation(b, conflictGroup));
        }

        Comparator<ApprovalRecommendation> cmp = Comparator
                .comparing(ApprovalRecommendation::isValid)
                .reversed()
                .thenComparing(
                        ApprovalRecommendation::getPriorityLevel,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        ApprovalRecommendation::getCourseNeed,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        ApprovalRecommendation::getTimeFairness,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        ApprovalRecommendation::getRoomFairness,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        ApprovalRecommendation::getSuitability,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        ApprovalRecommendation::getAlternativeScarcity,
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        a -> a.getBooking().getCreatedAt()
                );

        list.sort(cmp);


        return list;
    }


    private ApprovalRecommendation buildRecommendation(Booking b, boolean conflictGroup) {

        BookingCandidate c = new BookingCandidate(
                b.getRequester(),
                b.getCourse(),
                b.getRoom(),
                b.getType(),
                b.getDate(),
                b.getStartTime(),
                b.getEndTime(),
                b.getExpectedStudents(),
                b.getRequiredFacilities(),
                b.getId()
        );

        ValidationResult vr = validation.validate(c, false);

        RoomRecommendation rs = scoring.score(c);

        int pri = b.getType().isAcademic() ? 2 : 1;

        double need = scoring.courseNeed(c);
        double time = scoring.timeFairness(c);
        double roomFair = scoring.roomFairness(c);

        double suit =
                (rs.getFacilityScore()
                        + rs.getCapacityScore()) / 2.0;

        // Alternatives are expensive to calculate because every candidate
        // must pass the full booking validator and scoring pipeline.
        //
        // For a valid non-conflicting request, alternatives are not needed at all.
        // For a conflict we keep up to 3 because scarcity is part of comparison.
        // For an invalid single request we only need the best alternative to show
        // the administrator a useful next action.
        List<AlternativeOption> alts;
        if (conflictGroup) {
            alts = alternativeService.findAlternatives(b, 3);
        } else if (!vr.valid()) {
            alts = alternativeService.findAlternatives(b, 1);
        } else {
            alts = List.of();
        }

        double scarcity = alts.isEmpty()
                ? 100
                : Math.max(
                        10,
                        100 - alts.size() * 25.0
                );

        double display =
                (pri == 2 ? 30 : 10)
                        + need * .20
                        + time * .15
                        + roomFair * .10
                        + suit * .15
                        + scarcity * .10;

        display = Math.min(
                100,
                Math.round(display * 10.0) / 10.0
        );

        String alt = alts.isEmpty()
                ? (vr.valid() && !conflictGroup
                    ? "Alternative not required"
                    : "No suitable alternative found")
                : alts.get(0).room().getCode()
                    + " on "
                    + alts.get(0).date()
                    + " "
                    + alts.get(0).startTime()
                    + "-"
                    + alts.get(0).endTime()
                    + " ("
                    + alts.get(0).score()
                    + "%)";

        String exp = vr.valid()
                ? (b.getType().isAcademic()
                    ? "Academic priority (LAB/TUTORIAL). "
                    : "Lower ECA priority. ")
                    + "Course need "
                    + Math.round(need)
                    + "%, time fairness "
                    + Math.round(time)
                    + "%, room fairness "
                    + Math.round(roomFair)
                    + "%, suitability "
                    + Math.round(suit)
                    + "%. "
                    + (!conflictGroup
                        ? "No competing request currently requires conflict ranking."
                        : (alts.isEmpty()
                            ? "No workable alternative was found."
                            : "A workable alternative exists."))
                : "Invalid due to hard constraints: "
                    + String.join(
                            " ",
                            vr.violations()
                    );

        return ApprovalRecommendation.builder()
                .booking(b)
                .valid(vr.valid())
                .priorityLevel(pri)
                .courseNeed(need)
                .timeFairness(time)
                .roomFairness(roomFair)
                .suitability(suit)
                .alternativeScarcity(scarcity)
                .displayScore(display)
                .explanation(exp)
                .suggestedAlternative(alt)
                .build();
    }


    @Transactional
    public void approve(Long id, String reason) {

        Booking b =
                bookings.findById(id).orElseThrow();

        rooms.findByIdForUpdate(
                b.getRoom().getId()
        ).orElseThrow();

        BookingCandidate c = new BookingCandidate(
                b.getRequester(),
                b.getCourse(),
                b.getRoom(),
                b.getType(),
                b.getDate(),
                b.getStartTime(),
                b.getEndTime(),
                b.getExpectedStudents(),
                b.getRequiredFacilities(),
                b.getId()
        );

        ValidationResult vr =
                validation.validate(c, false);

        if (!vr.valid()) {

            throw new IllegalArgumentException(
                    "Cannot approve: "
                            + String.join(
                                    " ",
                                    vr.violations()
                            )
            );
        }

        List<Booking> competitors =
                bookings.findRoomOverlaps(
                        b.getRoom().getId(),
                        b.getDate(),
                        b.getStartTime(),
                        b.getEndTime(),
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.ALTERNATIVE_PROPOSED
                        )
                );

        b.setStatus(BookingStatus.APPROVED);

        b.setAdminDecisionReason(
                reason == null || reason.isBlank()
                        ? "Approved by administrator"
                        : reason
        );

        bookings.save(b);

        notifications.notify(
                b.getRequester(),
                "Booking approved",
                summary(b),
                "APPROVED-" + b.getId()
        );

        audit.log(
                "BOOKING_APPROVED",
                "Booking",
                b.getId(),
                b.getAdminDecisionReason()
        );

        processCompetitors(b, competitors);
    }


    @Transactional
    public void reject(Long id, String reason) {

        Booking b =
                bookings.findById(id).orElseThrow();

        b.setStatus(BookingStatus.REJECTED);

        b.setAdminDecisionReason(
                reason == null || reason.isBlank()
                        ? "Rejected by administrator"
                        : reason
        );

        bookings.save(b);

        notifications.notify(
                b.getRequester(),
                "Booking rejected",
                b.getAdminDecisionReason(),
                "REJECTED-" + b.getId()
        );

        audit.log(
                "BOOKING_REJECTED",
                "Booking",
                b.getId(),
                b.getAdminDecisionReason()
        );
    }


    @Transactional
    public void approveWithOverride(
            Long id,
            String reason) {

        if (!settings.getBoolean(
                "admin.override.enabled",
                true)) {

            throw new IllegalArgumentException(
                    "Admin override is disabled in settings."
            );
        }

        if (reason == null || reason.isBlank()) {

            throw new IllegalArgumentException(
                    "An override reason is required."
            );
        }

        Booking b =
                bookings.findById(id).orElseThrow();

        rooms.findByIdForUpdate(
                b.getRoom().getId()
        ).orElseThrow();

        BookingCandidate c = new BookingCandidate(
                b.getRequester(),
                b.getCourse(),
                b.getRoom(),
                b.getType(),
                b.getDate(),
                b.getStartTime(),
                b.getEndTime(),
                b.getExpectedStudents(),
                b.getRequiredFacilities(),
                b.getId()
        );

        List<Booking> competitors =
                bookings.findRoomOverlaps(
                        b.getRoom().getId(),
                        b.getDate(),
                        b.getStartTime(),
                        b.getEndTime(),
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.ALTERNATIVE_PROPOSED
                        )
                );

        ValidationResult vr =
                validation.validate(c, false);

        List<String> nonOverride =
                vr.violations()
                        .stream()
                        .filter(x ->
                                !(x.contains(
                                        "weekly booking allowance")
                                        || x.contains(
                                        "daily booking allowance")
                                        || x.contains(
                                        "weekly booking limit")
                                        || x.contains(
                                        "prime-time allowance"))
                        )
                        .toList();

        if (!nonOverride.isEmpty()) {

            throw new IllegalArgumentException(
                    "Override not allowed for safety/access/conflict rule(s): "
                            + String.join(
                                    " ",
                                    nonOverride
                            )
            );
        }

        b.setOverrideReason(reason);

        b.setStatus(
                BookingStatus.APPROVED
        );

        b.setAdminDecisionReason(
                "Approved with administrative override"
        );

        bookings.save(b);

        notifications.notify(
                b.getRequester(),
                "Booking approved with override",
                summary(b),
                "OVERRIDE-" + b.getId()
        );

        audit.log(
                "ADMIN_OVERRIDE",
                "Booking",
                b.getId(),
                reason
        );

        processCompetitors(
                b,
                competitors
        );
    }


    @Transactional
    public void suggestAlternative(Long id) {

        Booking b =
                bookings.findById(id).orElseThrow();

        if (b.getStatus() == BookingStatus.CANCELLED
                || b.getStatus() == BookingStatus.REJECTED) {

            throw new IllegalArgumentException(
                    "A closed booking cannot be given an alternative."
            );
        }

        List<AlternativeOption> options =
                alternativeService.findAlternatives(
                        b,
                        1
                );

        if (options.isEmpty()) {

            throw new IllegalArgumentException(
                    "No suitable alternative is currently available."
            );
        }

        AlternativeOption option =
                options.getFirst();

        AlternativeSuggestion suggestion =
                new AlternativeSuggestion();

        suggestion.setBooking(b);
        suggestion.setRoom(option.room());
        suggestion.setDate(option.date());
        suggestion.setStartTime(option.startTime());
        suggestion.setEndTime(option.endTime());
        suggestion.setScore(option.score());
        suggestion.setExplanation(
                option.explanation()
        );

        alternatives.save(suggestion);

        b.setStatus(
                BookingStatus.ALTERNATIVE_PROPOSED
        );

        b.setAdminDecisionReason(
                "Alternative booking suggested; awaiting requester decision."
        );

        bookings.save(b);

        notifications.notify(
                b.getRequester(),
                "Alternative booking suggested",
                "Suggested: "
                        + option.room().getCode()
                        + " on "
                        + option.date()
                        + " "
                        + option.startTime()
                        + "-"
                        + option.endTime()
                        + ".",
                "ADMIN-ALTERNATIVE-"
                        + b.getId()
                        + "-"
                        + suggestion.getId()
        );

        audit.log(
                "ALTERNATIVE_SUGGESTED",
                "Booking",
                b.getId(),
                option.explanation()
        );
    }


    private void processCompetitors(
            Booking approved,
            List<Booking> competitors) {

        for (Booking other : competitors) {

            if (other.getId().equals(
                    approved.getId())
                    || other.getStatus()
                    == BookingStatus.APPROVED) {

                continue;
            }

            List<AlternativeOption> opts =
                    alternativeService.findAlternatives(
                            other,
                            1
                    );

            if (opts.isEmpty()) {

                other.setStatus(
                        BookingStatus.REJECTED
                );

                other.setAdminDecisionReason(
                        "Competing request approved and no suitable alternative was available."
                );

                bookings.save(other);

                notifications.notify(
                        other.getRequester(),
                        "Booking request unsuccessful",
                        other.getAdminDecisionReason(),
                        "CONFLICT-REJECT-"
                                + other.getId()
                );

            } else {

                AlternativeOption o =
                        opts.get(0);

                AlternativeSuggestion suggestion =
                        new AlternativeSuggestion();

                suggestion.setBooking(other);
                suggestion.setRoom(o.room());
                suggestion.setDate(o.date());
                suggestion.setStartTime(
                        o.startTime()
                );
                suggestion.setEndTime(
                        o.endTime()
                );
                suggestion.setScore(
                        o.score()
                );
                suggestion.setExplanation(
                        o.explanation()
                );

                alternatives.save(
                        suggestion
                );

                other.setStatus(
                        BookingStatus.ALTERNATIVE_PROPOSED
                );

                other.setAdminDecisionReason(
                        "Competing request approved; alternative proposed."
                );

                bookings.save(other);

                notifications.notify(
                        other.getRequester(),
                        "Alternative booking suggested",
                        "Original room/time was allocated to another request. "
                                + "Suggested: "
                                + o.room().getCode()
                                + " on "
                                + o.date()
                                + " "
                                + o.startTime()
                                + "-"
                                + o.endTime()
                                + ".",
                        "ALTERNATIVE-"
                                + other.getId()
                                + "-"
                                + suggestion.getId()
                );

                audit.log(
                        "ALTERNATIVE_SUGGESTED",
                        "Booking",
                        other.getId(),
                        o.explanation()
                );
            }
        }
    }


    private String summary(Booking b) {

        return b.getType()
                + " booking in "
                + b.getRoom().getCode()
                + " on "
                + b.getDate()
                + " from "
                + b.getStartTime()
                + " to "
                + b.getEndTime()
                + ".";
    }
}