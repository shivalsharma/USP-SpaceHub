package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor
public class SearchService {
    private final CourseRepository courses;
    private final RoomRepository rooms;
    private final BookingValidationService validation;
    private final ScoringService scoring;
    private final SettingsService settings;
    private final AuditService audit;

    public SearchResult search(UserAccount user, SearchForm f) {
        Course course = f.getCourseId() == null ? null : courses.findById(f.getCourseId()).orElse(null);
        Set<String> req = FacilityUtil.parseCsv(f.getRequiredFacilitiesCsv());
        Set<String> optional = FacilityUtil.parseCsv(f.getOptionalFacilitiesCsv());
        List<String> global = new ArrayList<>();
        if (f.getBookingType() != null && f.getBookingType().isAcademic() && course == null)
            global.add("Select a valid course for LAB or TUTORIAL bookings.");
        if (f.getEndTime() != null && f.getStartTime() != null && !f.getEndTime().isAfter(f.getStartTime()))
            global.add("End time must be after start time.");
        List<RoomRecommendation> valid = new ArrayList<>();
        Map<Long, List<String>> excluded = new LinkedHashMap<>();
        for (Room room : rooms.findAllByOrderByCode()) {
            BookingCandidate c = new BookingCandidate(user, course, room, f.getBookingType(), f.getDate(), f.getStartTime(), f.getEndTime(), f.getExpectedStudents(), req, null);
            ValidationResult vr = validation.validate(c, true);
            if (vr.valid()) valid.add(applyPreferences(scoring.score(c), f.getPreferredBuilding(), optional));
            else excluded.put(room.getId(), vr.violations());
        }
        valid.sort(Comparator.comparingDouble(RoomRecommendation::getScore).reversed().thenComparing(x -> x.getRoom().getCode()));
        int max = settings.getInt("search.result.count", 8);
        if (valid.size() > max) valid = new ArrayList<>(valid.subList(0, max));
        List<SearchAlternative> alternatives = findFlexibleAlternatives(user, course, f, req, optional, max);
        if (valid.isEmpty() && alternatives.isEmpty() && global.isEmpty())
            global.add("No room passed all hard constraints for the requested slot. Enable flexible time/date or adjust requirements.");
        if (!valid.isEmpty()) audit.log("SEARCH_RECOMMENDATION", "User", user.getId(), "Top room " + valid.getFirst().getRoom().getCode() + " score=" + valid.getFirst().getScore() + " for " + f.getBookingType() + " on " + f.getDate() + " " + f.getStartTime() + "-" + f.getEndTime());
        else if (!alternatives.isEmpty()) audit.log("SEARCH_ALTERNATIVE_RECOMMENDATION", "User", user.getId(), "No exact room; top flexible alternative " + alternatives.getFirst().getRoom().getCode() + " score=" + alternatives.getFirst().getScore());
        else audit.log("SEARCH_NO_RESULT", "User", user.getId(), "No valid room for " + f.getBookingType() + " on " + f.getDate() + " " + f.getStartTime() + "-" + f.getEndTime());
        return SearchResult.builder().recommendations(valid).alternatives(alternatives).globalViolations(global).excludedRoomReasons(excluded).build();
    }

    private RoomRecommendation applyPreferences(RoomRecommendation rec, String preferred, Set<String> optional) {
        double bonus = 0;
        if (preferred != null && !preferred.isBlank() && rec.getRoom().getBuilding().equalsIgnoreCase(preferred.trim())) {
            bonus += 3;
            rec.getReasons().add("Matches the preferred building.");
        }
        Set<String> available = FacilityUtil.allFacilities(rec.getRoom());
        Set<String> faulty = FacilityUtil.faulty(rec.getRoom());
        int matched = 0;
        for (String f : optional) if (available.contains(f) && !faulty.contains(f)) matched++;
        if (matched > 0) {
            bonus += Math.min(5, matched * 1.5);
            rec.getReasons().add("Provides " + matched + " optional preferred facilit" + (matched == 1 ? "y" : "ies") + ".");
        }
        if (bonus > 0) rec.setScore(Math.min(100, Math.round((rec.getScore() + bonus) * 10.0) / 10.0));
        return rec;
    }

    private List<SearchAlternative> findFlexibleAlternatives(UserAccount user, Course course, SearchForm f, Set<String> req, Set<String> optional, int max) {
        if (f.getDate() == null || f.getStartTime() == null || f.getEndTime() == null) return List.of();
        if (f.getFlexibilityHours() <= 0 && !f.isFlexibleDate()) return List.of();
        long duration = ChronoUnit.MINUTES.between(f.getStartTime(), f.getEndTime());
        List<Integer> dayOffsets = new ArrayList<>(List.of(0));
        if (f.isFlexibleDate()) dayOffsets.addAll(List.of(1, -1, 2, -2));
        List<Integer> shifts = new ArrayList<>();
        int flex = Math.max(0, f.getFlexibilityHours());
        shifts.add(0);
        if (flex > 0) for (int h = 1; h <= flex; h++) { shifts.add(h); shifts.add(-h); }
        List<SearchAlternative> out = new ArrayList<>();
        for (int day : dayOffsets) {
            for (int shift : shifts) {
                if (day == 0 && shift == 0) continue;
                LocalDate date = f.getDate().plusDays(day);
                LocalTime start = f.getStartTime().plusHours(shift), end = start.plusMinutes(duration);
                for (Room room : rooms.findAllByOrderByCode()) {
                    BookingCandidate c = new BookingCandidate(user, course, room, f.getBookingType(), date, start, end, f.getExpectedStudents(), req, null);
                    ValidationResult vr = validation.validate(c, true);
                    if (!vr.valid()) continue;
                    RoomRecommendation rr = applyPreferences(scoring.score(c), f.getPreferredBuilding(), optional);
                    double disruption = Math.max(0, 100 - Math.abs(day) * 8 - Math.abs(shift) * 5);
                    double finalScore = Math.round((rr.getScore() * .80 + disruption * .20) * 10.0) / 10.0;
                    List<String> reasons = new ArrayList<>(rr.getReasons());
                    if (day != 0) reasons.add("Nearby date shift of " + Math.abs(day) + " day(s).");
                    if (shift != 0) reasons.add("Time shifted by " + Math.abs(shift) + " hour(s) to satisfy availability.");
                    out.add(SearchAlternative.builder().room(room).date(date).startTime(start).endTime(end).score(finalScore).reasons(reasons).build());
                }
            }
        }
        Map<String, SearchAlternative> unique = new LinkedHashMap<>();
        out.stream().sorted(Comparator.comparingDouble(SearchAlternative::getScore).reversed())
                .forEach(a -> unique.putIfAbsent(a.getRoom().getId() + "|" + a.getDate() + "|" + a.getStartTime(), a));
        return unique.values().stream().limit(max).toList();
    }
}