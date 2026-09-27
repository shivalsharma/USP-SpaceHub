package fj.ac.usp.spacehub.service;

import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseRepository courses;
    private final RoomRepository rooms;
    private final BookingValidationService validation;
    private final ScoringService scoring;
    private final SettingsService settings;
    private final AuditService audit;

    /**
     * Runs the optimized room search.
     *
     * A transaction is kept open because Course/Room relationships such as
     * lecturers and facilities may be LAZY-loaded during validation/scoring.
     * Audit logging also writes to the database, so this is intentionally not
     * marked readOnly.
     */
    @Transactional
    public SearchResult search(UserAccount user, SearchForm f) {
        Course course = f.getCourseId() == null
                ? null
                : courses.findById(f.getCourseId()).orElse(null);

        Set<String> required = FacilityUtil.parseCsv(f.getRequiredFacilitiesCsv());
        Set<String> optional = FacilityUtil.parseCsv(f.getOptionalFacilitiesCsv());

        List<String> global = new ArrayList<>();
        Map<Long, List<String>> excluded = new LinkedHashMap<>();
        List<RoomRecommendation> valid = new ArrayList<>();

        // ---------- Basic request validation ----------
        if (f.getBookingType() == null) {
            global.add("Select a booking type.");
        }

        boolean academic = f.getBookingType() != null && f.getBookingType().isAcademic();

        if (academic && course == null) {
            global.add("Select a valid course for LAB or TUTORIAL bookings.");
        }

        if (f.getDate() == null || f.getStartTime() == null || f.getEndTime() == null) {
            global.add("Date, start time and end time are required.");
        } else if (!f.getEndTime().isAfter(f.getStartTime())) {
            global.add("End time must be after start time.");
        }

        // LAB/TUTORIAL must use the course roll from the Course entity.
        // ECA uses the attendance entered in the form.
        int expectedStudents = f.getExpectedStudents();

        if (academic && course != null) {
            int courseRoll = course.getCourseRoll();
            if (courseRoll < 1) {
                global.add("The selected course does not have a valid course roll. Ask an administrator to update the course before booking.");
            } else {
                expectedStudents = courseRoll;
                // Keep the form/result state consistent with the value actually used.
                f.setExpectedStudents(courseRoll);
            }
        } else if (!academic && expectedStudents < 1) {
            global.add("Expected attendance must be at least 1 for an ECA booking.");
        }

        // Do not continue into room validation/scoring when the request itself
        // is incomplete/invalid. This avoids avoidable server errors and gives
        // the user a clear validation message instead.
        if (!global.isEmpty()) {
            return SearchResult.builder()
                    .recommendations(valid)
                    .alternatives(List.of())
                    .globalViolations(global)
                    .excludedRoomReasons(excluded)
                    .build();
        }

        // Load once and reuse for exact and flexible searches.
        List<Room> allRooms = rooms.findAllByOrderByCode();

        // ---------- Hard constraints, then ranking ----------
        for (Room room : allRooms) {
            BookingCandidate candidate = new BookingCandidate(
                    user,
                    course,
                    room,
                    f.getBookingType(),
                    f.getDate(),
                    f.getStartTime(),
                    f.getEndTime(),
                    expectedStudents,
                    required,
                    null
            );

            ValidationResult result = validation.validate(candidate, true);

            if (result.valid()) {
                RoomRecommendation recommendation = scoring.score(candidate);
                valid.add(applyPreferences(
                        recommendation,
                        f.getPreferredBuilding(),
                        optional
                ));
            } else {
                excluded.put(room.getId(), result.violations());
            }
        }

        valid.sort(
                Comparator.comparingDouble(RoomRecommendation::getScore)
                        .reversed()
                        .thenComparing(r -> Optional.ofNullable(r.getRoom().getCode()).orElse(""))
        );

        int max = Math.max(1, settings.getInt("search.result.count", 8));
        if (valid.size() > max) {
            valid = new ArrayList<>(valid.subList(0, max));
        }

        List<SearchAlternative> alternatives = findFlexibleAlternatives(
                user,
                course,
                f,
                required,
                optional,
                expectedStudents,
                max,
                allRooms
        );

        if (valid.isEmpty() && alternatives.isEmpty()) {
            global.add("No room passed all hard constraints for the requested slot. Try a flexible time/date or adjust the optional preferences.");
        }

        // ---------- Audit ----------
        if (!valid.isEmpty()) {
            RoomRecommendation top = valid.getFirst();
            audit.log(
                    "SEARCH_RECOMMENDATION",
                    "User",
                    user.getId(),
                    "Top room " + top.getRoom().getCode()
                            + " score=" + top.getScore()
                            + " for " + f.getBookingType()
                            + " on " + f.getDate()
                            + " " + f.getStartTime() + "-" + f.getEndTime()
            );
        } else if (!alternatives.isEmpty()) {
            SearchAlternative top = alternatives.getFirst();
            audit.log(
                    "SEARCH_ALTERNATIVE_RECOMMENDATION",
                    "User",
                    user.getId(),
                    "No exact room; top flexible alternative "
                            + top.getRoom().getCode()
                            + " score=" + top.getScore()
            );
        } else {
            audit.log(
                    "SEARCH_NO_RESULT",
                    "User",
                    user.getId(),
                    "No valid room for " + f.getBookingType()
                            + " on " + f.getDate()
                            + " " + f.getStartTime() + "-" + f.getEndTime()
            );
        }

        return SearchResult.builder()
                .recommendations(valid)
                .alternatives(alternatives)
                .globalViolations(global)
                .excludedRoomReasons(excluded)
                .build();
    }

    private RoomRecommendation applyPreferences(
            RoomRecommendation recommendation,
            String preferredBuilding,
            Set<String> optionalFacilities
    ) {
        double bonus = 0;

        Room room = recommendation.getRoom();
        String roomBuilding = room == null ? null : room.getBuilding();

        // Null-safe preferred building comparison.
        if (preferredBuilding != null
                && !preferredBuilding.isBlank()
                && roomBuilding != null
                && roomBuilding.equalsIgnoreCase(preferredBuilding.trim())) {

            bonus += 3;
            recommendation.getReasons().add("Matches the preferred building.");
        }

        if (room != null && optionalFacilities != null && !optionalFacilities.isEmpty()) {
            Set<String> available = FacilityUtil.allFacilities(room);
            Set<String> faulty = FacilityUtil.faulty(room);

            int matched = 0;
            for (String facility : optionalFacilities) {
                if (available.contains(facility) && !faulty.contains(facility)) {
                    matched++;
                }
            }

            if (matched > 0) {
                bonus += Math.min(5, matched * 1.5);
                recommendation.getReasons().add(
                        "Provides " + matched + " optional preferred facilit"
                                + (matched == 1 ? "y" : "ies") + "."
                );
            }
        }

        if (bonus > 0) {
            recommendation.setScore(
                    Math.min(
                            100,
                            Math.round((recommendation.getScore() + bonus) * 10.0) / 10.0
                    )
            );
        }

        return recommendation;
    }

    private List<SearchAlternative> findFlexibleAlternatives(
            UserAccount user,
            Course course,
            SearchForm f,
            Set<String> required,
            Set<String> optional,
            int expectedStudents,
            int max,
            List<Room> allRooms
    ) {
        if (f.getDate() == null || f.getStartTime() == null || f.getEndTime() == null) {
            return List.of();
        }

        if (f.getFlexibilityHours() <= 0 && !f.isFlexibleDate()) {
            return List.of();
        }

        long durationMinutes = ChronoUnit.MINUTES.between(
                f.getStartTime(),
                f.getEndTime()
        );

        if (durationMinutes <= 0) {
            return List.of();
        }

        List<Integer> dayOffsets = new ArrayList<>();
        dayOffsets.add(0);
        if (f.isFlexibleDate()) {
            dayOffsets.addAll(List.of(1, -1, 2, -2));
        }

        List<Integer> shifts = new ArrayList<>();
        shifts.add(0);

        int flexibility = Math.max(0, f.getFlexibilityHours());
        for (int hour = 1; hour <= flexibility; hour++) {
            // Prefer the smallest changes first.
            shifts.add(hour);
            shifts.add(-hour);
        }

        List<SearchAlternative> alternatives = new ArrayList<>();

        for (int dayOffset : dayOffsets) {
            LocalDate alternativeDate = f.getDate().plusDays(dayOffset);

            for (int shift : shifts) {
                if (dayOffset == 0 && shift == 0) {
                    continue; // exact request was already checked above
                }

                LocalTime alternativeStart = f.getStartTime().plusHours(shift);
                LocalTime alternativeEnd = alternativeStart.plusMinutes(durationMinutes);

                for (Room room : allRooms) {
                    BookingCandidate candidate = new BookingCandidate(
                            user,
                            course,
                            room,
                            f.getBookingType(),
                            alternativeDate,
                            alternativeStart,
                            alternativeEnd,
                            expectedStudents,
                            required,
                            null
                    );

                    ValidationResult result = validation.validate(candidate, true);
                    if (!result.valid()) {
                        continue;
                    }

                    RoomRecommendation recommendation = applyPreferences(
                            scoring.score(candidate),
                            f.getPreferredBuilding(),
                            optional
                    );

                    double disruptionScore = Math.max(
                            0,
                            100 - Math.abs(dayOffset) * 8 - Math.abs(shift) * 5
                    );

                    double finalScore = Math.round(
                            (recommendation.getScore() * 0.80
                                    + disruptionScore * 0.20) * 10.0
                    ) / 10.0;

                    List<String> reasons = new ArrayList<>(recommendation.getReasons());

                    if (dayOffset != 0) {
                        reasons.add(
                                "Nearby date shift of "
                                        + Math.abs(dayOffset)
                                        + " day(s)."
                        );
                    }

                    if (shift != 0) {
                        reasons.add(
                                "Time shifted by "
                                        + Math.abs(shift)
                                        + " hour(s) to satisfy availability."
                        );
                    }

                    alternatives.add(
                            SearchAlternative.builder()
                                    .room(room)
                                    .date(alternativeDate)
                                    .startTime(alternativeStart)
                                    .endTime(alternativeEnd)
                                    .score(finalScore)
                                    .reasons(reasons)
                                    .build()
                    );
                }
            }
        }

        // Remove duplicate room/date/start combinations and return the best ones.
        Map<String, SearchAlternative> unique = new LinkedHashMap<>();

        alternatives.stream()
                .sorted(
                        Comparator.comparingDouble(SearchAlternative::getScore)
                                .reversed()
                                .thenComparing(a -> Math.abs(
                                        ChronoUnit.DAYS.between(f.getDate(), a.getDate())
                                ))
                                .thenComparing(a -> Math.abs(
                                        ChronoUnit.MINUTES.between(f.getStartTime(), a.getStartTime())
                                ))
                                .thenComparing(a -> Optional.ofNullable(a.getRoom().getCode()).orElse(""))
                )
                .forEach(a -> unique.putIfAbsent(
                        a.getRoom().getId()
                                + "|" + a.getDate()
                                + "|" + a.getStartTime(),
                        a
                ));

        return unique.values().stream()
                .limit(max)
                .toList();
    }
}
