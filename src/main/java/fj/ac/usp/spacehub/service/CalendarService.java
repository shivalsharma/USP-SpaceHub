package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.CalendarDay;
import fj.ac.usp.spacehub.dto.CalendarEntry;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.BookingRepository;
import fj.ac.usp.spacehub.repository.ScheduleEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service @RequiredArgsConstructor
public class CalendarService {
    private static final ZoneId FIJI = ZoneId.of("Pacific/Fiji");
    private final BookingRepository bookings;
    private final ScheduleEntryRepository schedules;

    public List<CalendarDay> month(YearMonth month, UserAccount user, Long roomId) {
        LocalDate first = month.atDay(1), last = month.atEndOfMonth();
        LocalDate gridStart = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate gridEnd = last.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Booking> all = bookings.findByDateBetweenAndStatus(gridStart, gridEnd, BookingStatus.APPROVED).stream()
                .filter(b -> roomId == null || b.getRoom().getId().equals(roomId))
                .filter(b -> roomId != null || user == null || user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.IT_ADMIN || b.getRequester().getId().equals(user.getId()))
                .toList();
        Map<LocalDate, List<CalendarEntry>> by = new HashMap<>();
        for (Booking b : all) by.computeIfAbsent(b.getDate(), x -> new ArrayList<>()).add(CalendarEntry.of(b));

        // "My calendar" view: also show the busy/timetable times the user added on their profile.
        if (roomId == null && user != null) {
            List<Booking> mine = all.stream().filter(b -> b.getRequester().getId().equals(user.getId())).toList();
            for (ScheduleEntry s : schedules.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(user.getId(), gridStart, gridEnd)) {
                boolean clash = mine.stream().anyMatch(b -> b.getDate().equals(s.getDate())
                        && b.getStartTime().isBefore(s.getEndTime()) && b.getEndTime().isAfter(s.getStartTime()));
                by.computeIfAbsent(s.getDate(), x -> new ArrayList<>()).add(CalendarEntry.of(s, clash));
            }
        }

        by.values().forEach(l -> l.sort(Comparator.comparing(CalendarEntry::startTime)
                .thenComparing(CalendarEntry::endTime)
                .thenComparing(CalendarEntry::isBusy)));
        LocalDate today = LocalDate.now(FIJI);
        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1))
            days.add(new CalendarDay(d, d.getMonth() == month.getMonth(), d.equals(today), by.getOrDefault(d, List.of())));
        return days;
    }
}
