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
        List<AlternativeOption> out = new ArrayList<>();
        long mins = ChronoUnit.MINUTES.between(b.getStartTime(), b.getEndTime());
        List<Room> active = rooms.findByStatusOrderByCode(RoomStatus.ACTIVE);
        // Same time, similar room
        for (Room r : active) if (!r.getId().equals(b.getRoom().getId()))
            addIfValid(out, b, r, b.getDate(), b.getStartTime(), b.getEndTime(), 98, "Same time in a different suitable room");
        // Same room, nearby time
        for (int shift : new int[]{1, -1, 2, -2}) {
            LocalTime s = b.getStartTime().plusHours(shift), e = s.plusMinutes(mins);
            addIfValid(out, b, b.getRoom(), b.getDate(), s, e, 92 - Math.abs(shift) * 3, "Same room with a nearby time shift of " + Math.abs(shift) + " hour(s)");
        }
        // Similar room, nearby time
        for (int shift : new int[]{1, -1, 2, -2})
            for (Room r : active) if (!r.getId().equals(b.getRoom().getId())) {
                LocalTime s = b.getStartTime().plusHours(shift), e = s.plusMinutes(mins);
                addIfValid(out, b, r, b.getDate(), s, e, 86 - Math.abs(shift) * 3, "Suitable room with a nearby time shift");
            }
        // Similar day/time combinations
        for (int day : new int[]{1, -1, 2, -2})
            for (Room r : active) {
                LocalDate d = b.getDate().plusDays(day);
                addIfValid(out, b, r, d, b.getStartTime(), b.getEndTime(), 78 - Math.abs(day) * 2, "Similar time on a nearby day");
            }
        Map<String, AlternativeOption> uniq = new LinkedHashMap<>();
        out.stream().sorted(Comparator.comparingDouble(AlternativeOption::score).reversed())
                .forEach(a -> uniq.putIfAbsent(a.room().getId() + "|" + a.date() + "|" + a.startTime() + "|" + a.endTime(), a));
        return uniq.values().stream().limit(limit).toList();
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