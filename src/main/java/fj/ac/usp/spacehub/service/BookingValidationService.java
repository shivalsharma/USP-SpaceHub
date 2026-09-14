package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class BookingValidationService {
    private static final List<BookingStatus> QUOTA_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.APPROVED, BookingStatus.ALTERNATIVE_PROPOSED);
    private final BookingRepository bookings;
    private final ScheduleEntryRepository schedules;
    private final MaintenanceRepository maintenance;
    private final SettingsService settings;

    public ValidationResult validate(BookingCandidate c, boolean checkDuplicate) {
        List<String> v = new ArrayList<>();
        UserAccount u = c.requester();
        Room r = c.room();
        Course course = c.course();
        if (u == null || !u.isActive()) v.add("Your user account is disabled or unavailable.");
        if (c.type() == null) v.add("A booking type is required.");
        if (c.date() == null || c.startTime() == null || c.endTime() == null)
            v.add("Date, start time and end time are required.");
        if (!v.isEmpty()) return ValidationResult.fail(v);
        if (!c.endTime().isAfter(c.startTime())) v.add("End time must be after start time.");
        else {
            long mins = ChronoUnit.MINUTES.between(c.startTime(), c.endTime());
            int maxHours = settings.getInt("booking.max.duration.hours", 4);
            if (mins > maxHours * 60L) v.add("Booking duration exceeds the configured maximum of " + maxHours + " consecutive hour(s).");
        }
        LocalTime dayStart = settings.getTime("booking.day.start", LocalTime.of(8, 0));
        LocalTime dayEnd = settings.getTime("booking.day.end", LocalTime.of(22, 0));
        if (c.startTime().isBefore(dayStart) || c.endTime().isAfter(dayEnd))
            v.add("Bookings must be within " + dayStart + " and " + dayEnd + ".");
        LocalDate today = LocalDate.now(ZoneId.of("Pacific/Fiji"));
        if (c.date().isBefore(today)) v.add("Bookings cannot be made in the past.");
        if (c.date().isAfter(today.plusDays(settings.getInt("booking.horizon.days", 120))))
            v.add("The requested date is beyond the booking horizon.");
        if (c.expectedStudents() < 1) v.add("Expected students must be at least 1.");
        if (u != null && u.getRole() == UserRole.STUDENT && c.type() != BookingType.ECA)
            v.add("Students may only submit ECA booking requests.");
        if (u != null && u.getRole() != UserRole.STUDENT && u.getRole() != UserRole.LECTURER)
            v.add("Only lecturer/staff or student users may submit bookings.");
        if (c.type() != null && c.type().isAcademic()) {
            if (course == null) v.add("LAB and TUTORIAL bookings require a course.");
            else if (course.getLecturers().stream().noneMatch(x -> Objects.equals(x.getId(), u.getId())))
                v.add("You are not assigned to teach " + course.getCode() + ".");
        }
        if (r == null) { v.add("A room is required."); return ValidationResult.fail(v); }
        if (r.getStatus() != RoomStatus.ACTIVE) v.add("Room " + r.getCode() + " is not active.");
        if (r.getCapacity() < c.expectedStudents())
            v.add("Room capacity " + r.getCapacity() + " is below the expected class size of " + c.expectedStudents() + ".");
        if (!hasAccess(r, u, course)) v.add("You do not satisfy the room access restriction for " + r.getCode() + ".");
        if (!maintenance.findOverlaps(r.getId(), c.date(), c.startTime(), c.endTime()).isEmpty())
            v.add("The room is under maintenance during the requested time.");
        Set<String> req = new LinkedHashSet<>();
        if (course != null && course.getRequiredFacilities() != null)
            course.getRequiredFacilities().forEach(x -> req.add(FacilityUtil.normalize(x)));
        if (c.requiredFacilities() != null) c.requiredFacilities().forEach(x -> req.add(FacilityUtil.normalize(x)));
        Set<String> available = FacilityUtil.allFacilities(r), faulty = FacilityUtil.faulty(r);
        for (String f : req) {
            if (!available.contains(f)) v.add("Required facility " + f + " is not available in " + r.getCode() + ".");
            else if (faulty.contains(f)) v.add("Required facility " + f + " is currently faulty in " + r.getCode() + ".");
        }
        if (req.contains("COMPUTER") && r.getOperationalComputers() < c.expectedStudents())
            v.add("Only " + r.getOperationalComputers() + " operational computers are available for " + c.expectedStudents() + " students.");
        List<Booking> approvedRoom = bookings.findRoomOverlapsExcluding(r.getId(), c.date(), c.startTime(), c.endTime(), List.of(BookingStatus.APPROVED), c.excludeBookingId());
        if (!approvedRoom.isEmpty()) v.add("The room already has an approved booking during the requested time.");
        if (u != null) {
            if (!schedules.findOverlaps(u.getId(), c.date(), c.startTime(), c.endTime()).isEmpty())
                v.add("The requested time overlaps your timetable/busy schedule.");
            List<Booking> userOverlap = bookings.findUserOverlapsExcluding(u.getId(), c.date(), c.startTime(), c.endTime(), QUOTA_STATUSES, c.excludeBookingId());
            if (!userOverlap.isEmpty()) v.add("You already have another active booking request or approved booking during this time.");
            validateLecturerAllowances(c, v);
        }
        if (course != null && c.type() != null && c.type().isAcademic()) validateCourseRules(c, v);
        if (checkDuplicate && u != null) {
            Long courseId = course == null ? null : course.getId();
            List<Booking> dups = bookings.findDuplicates(u.getId(), r.getId(), courseId, c.date(), c.startTime(), c.endTime(), c.type(), QUOTA_STATUSES);
            boolean other = dups.stream().anyMatch(b -> !Objects.equals(b.getId(), c.excludeBookingId()));
            if (other) v.add("An identical booking request already exists.");
        }
        return v.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(v);
    }

    private void validateCourseRules(BookingCandidate c, List<String> v) {
        Course course = c.course();
        LocalDate ws = weekStart(c.date()), we = ws.plusDays(6);
        long current = bookings.findByCourseIdAndDateBetweenAndStatusIn(course.getId(), ws, we, QUOTA_STATUSES).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId())).count();
        if (current >= course.getMaxWeeklyBookings())
            v.add(course.getCode() + " has reached its weekly booking limit of " + course.getMaxWeeklyBookings() + " sessions.");
        if (isPrime(c.startTime(), c.endTime())) {
            long prime = bookings.findByCourseIdAndDateBetweenAndStatusIn(course.getId(), ws, we, QUOTA_STATUSES).stream()
                    .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId()))
                    .filter(b -> isPrime(b.getStartTime(), b.getEndTime())).count();
            if (prime >= course.getPrimeTimeAllowance())
                v.add(course.getCode() + " has reached its prime-time allowance of " + course.getPrimeTimeAllowance() + " session(s) this week.");
        }
    }

    private void validateLecturerAllowances(BookingCandidate c, List<String> v) {
        UserAccount u = c.requester();
        LocalDate ws = weekStart(c.date()), we = ws.plusDays(6);
        int weekly = u.getMaxWeeklyBookings() != null ? u.getMaxWeeklyBookings() : settings.getInt("lecturer.max.weekly.bookings", 10);
        int daily = u.getMaxDailyBookings() != null ? u.getMaxDailyBookings() : settings.getInt("lecturer.max.daily.bookings", 3);
        long wc = bookings.findByRequesterIdAndDateBetweenAndStatusIn(u.getId(), ws, we, QUOTA_STATUSES).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId())).count();
        long dc = bookings.findByRequesterIdAndDateBetweenAndStatusIn(u.getId(), c.date(), c.date(), QUOTA_STATUSES).stream()
                .filter(b -> !Objects.equals(b.getId(), c.excludeBookingId())).count();
        if (wc >= weekly) v.add("You have reached your weekly booking allowance of " + weekly + " sessions.");
        if (dc >= daily) v.add("You have reached your daily booking allowance of " + daily + " sessions.");
    }

    public boolean hasAccess(Room r, UserAccount u, Course c) {
        if (u == null) return false;
        boolean roleOk = switch (r.getAccessLevel()) {
            case BOTH -> true;
            case LECTURER_ONLY -> u.getRole() == UserRole.LECTURER || u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.IT_ADMIN;
            case STUDENT_ONLY -> u.getRole() == UserRole.STUDENT || u.getRole() == UserRole.ADMIN;
        };
        if (!roleOk) return false;
        if (r.getDepartmentRestriction() != null && !r.getDepartmentRestriction().isBlank() && !r.getDepartmentRestriction().equalsIgnoreCase(Optional.ofNullable(u.getDepartment()).orElse("")))
            return false;
        if (r.getCourseRestriction() != null && !r.getCourseRestriction().isBlank() && (c == null || !r.getCourseRestriction().equalsIgnoreCase(c.getCode())))
            return false;
        return true;
    }

    public boolean isPrime(LocalTime start, LocalTime end) {
        LocalTime ps = settings.getTime("prime.time.start", LocalTime.of(8, 0));
        LocalTime pe = settings.getTime("prime.time.end", LocalTime.of(12, 0));
        return start.isBefore(pe) && end.isAfter(ps);
    }

    public static LocalDate weekStart(LocalDate d) { return d.minusDays(d.getDayOfWeek().getValue() - 1L); }

    public long pendingCompetition(Room room, LocalDate date, LocalTime start, LocalTime end) {
        return bookings.findRoomOverlaps(room.getId(), date, start, end, List.of(BookingStatus.PENDING, BookingStatus.ALTERNATIVE_PROPOSED)).size();
    }

    public Set<String> combinedRequirements(Course c, Set<String> requested) {
        Set<String> out = new LinkedHashSet<>();
        if (c != null && c.getRequiredFacilities() != null)
            c.getRequiredFacilities().forEach(x -> out.add(FacilityUtil.normalize(x)));
        if (requested != null) requested.forEach(x -> out.add(FacilityUtil.normalize(x)));
        return out;
    }
}