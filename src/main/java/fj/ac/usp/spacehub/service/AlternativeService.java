package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor
public class AlternativeService {
    private final RoomRepository rooms;
    private final BookingValidationService validation;
    private final ScoringService scoring;

    public List<AlternativeOption> findAlternatives(Booking b, int limit) {
        if (limit <= 0) return List.of();

        long mins = ChronoUnit.MINUTES.between(b.getStartTime(), b.getEndTime());
        List<Room> active = rooms.findByStatusOrderByCode(RoomStatus.ACTIVE);
        List<AlternativeOption> chosen = new ArrayList<>();

        /*
         * Search by disruption priority instead of evaluating every possible
         * room/date/time combination before applying the limit.
         *
         * Tier 1: same time, different suitable room
         * Tier 2: same room, nearby time
         * Tier 3: different suitable room, nearby time
         * Tier 4: nearby day/time
         *
         * This keeps the original business-rule ordering while preventing the
         * approval review page from running hundreds of validation/database
         * checks when only the best 1-3 alternatives are needed.
         */

        // Tier 1 - Same time, similar room
        List<AlternativeOption> tier = new ArrayList<>();
        for (Room r : active) {
            if (!r.getId().equals(b.getRoom().getId())) {
                addIfValid(
                        tier, b, r,
                        b.getDate(),
                        b.getStartTime(),
                        b.getEndTime(),
                        98,
                        "Same time in a different suitable room"
                );
            }
        }
        addBestFromTier(chosen, tier, limit);
        if (chosen.size() >= limit) return chosen;

        // Tier 2 - Same room, nearby time
        tier.clear();
        for (int shift : new int[]{1, -1, 2, -2}) {
            LocalTime s = b.getStartTime().plusHours(shift);
            LocalTime e = s.plusMinutes(mins);

            addIfValid(
                    tier, b, b.getRoom(),
                    b.getDate(),
                    s,
                    e,
                    92 - Math.abs(shift) * 3,
                    "Same room with a nearby time shift of "
                            + Math.abs(shift) + " hour(s)"
            );
        }
        addBestFromTier(chosen, tier, limit);
        if (chosen.size() >= limit) return chosen;

        // Tier 3 - Similar room, nearby time
        tier.clear();
        for (int shift : new int[]{1, -1, 2, -2}) {
            LocalTime s = b.getStartTime().plusHours(shift);
            LocalTime e = s.plusMinutes(mins);

            for (Room r : active) {
                if (!r.getId().equals(b.getRoom().getId())) {
                    addIfValid(
                            tier, b, r,
                            b.getDate(),
                            s,
                            e,
                            86 - Math.abs(shift) * 3,
                            "Suitable room with a nearby time shift"
                    );
                }
            }
        }
        addBestFromTier(chosen, tier, limit);
        if (chosen.size() >= limit) return chosen;

        // Tier 4 - Similar day/time combinations
        tier.clear();
        for (int day : new int[]{1, -1, 2, -2}) {
            LocalDate d = b.getDate().plusDays(day);

            for (Room r : active) {
                addIfValid(
                        tier, b, r,
                        d,
                        b.getStartTime(),
                        b.getEndTime(),
                        78 - Math.abs(day) * 2,
                        "Similar time on a nearby day"
                );
            }
        }
        addBestFromTier(chosen, tier, limit);

        return chosen;
    }

    private void addBestFromTier(
            List<AlternativeOption> chosen,
            List<AlternativeOption> tier,
            int limit) {

        tier.sort(
                Comparator.comparingDouble(AlternativeOption::score)
                        .reversed()
        );

        Set<String> existing = new HashSet<>();

        for (AlternativeOption a : chosen) {
            existing.add(alternativeKey(a));
        }

        for (AlternativeOption a : tier) {
            if (chosen.size() >= limit) return;

            if (existing.add(alternativeKey(a))) {
                chosen.add(a);
            }
        }
    }

    private String alternativeKey(AlternativeOption a) {
        return a.room().getId()
                + "|" + a.date()
                + "|" + a.startTime()
                + "|" + a.endTime();
    }

    private void addIfValid(List<AlternativeOption> out, Booking b, Room r, LocalDate date, LocalTime start, LocalTime end, double base, String explanation) {
        if (start.isBefore(LocalTime.MIDNIGHT) || end.isBefore(start)) return;
        BookingCandidate c = new BookingCandidate(b.getRequester(), b.getCourse(), r, b.getType(), date, start, end, b.getExpectedStudents(), b.getRequiredFacilities(), b.getId());
        ValidationResult vr = validation.validate(c, false);
        if (!vr.valid()) return;
        double quality = scoring.score(c).getScore();
        double finalScore = Math.round((base * 0.65 + quality * 0.35) * 10.0) / 10.0;
        out.add(new AlternativeOption(r, date, start, end, finalScore, explanation + "; " + r.getCode() + " passes all hard constraints."));
    }
}