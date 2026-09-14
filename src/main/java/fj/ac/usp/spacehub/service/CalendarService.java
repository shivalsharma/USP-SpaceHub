package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.CalendarDay;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service @RequiredArgsConstructor
public class CalendarService {
    private final BookingRepository bookings;

    public List<CalendarDay> month(YearMonth month, UserAccount user, Long roomId) {
        LocalDate first = month.atDay(1), last = month.atEndOfMonth();
        LocalDate gridStart = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate gridEnd = last.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Booking> all = bookings.findByDateBetweenAndStatus(gridStart, gridEnd, BookingStatus.APPROVED).stream()
                .filter(b -> roomId == null || b.getRoom().getId().equals(roomId))
                .filter(b -> roomId != null || user == null || user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.IT_ADMIN || b.getRequester().getId().equals(user.getId()))
                .toList();
        Map<LocalDate, List<Booking>> by = new HashMap<>();
        for (Booking b : all) by.computeIfAbsent(b.getDate(), x -> new ArrayList<>()).add(b);
        by.values().forEach(l -> l.sort(Comparator.comparing(Booking::getStartTime)));
        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1))
            days.add(new CalendarDay(d, d.getMonth() == month.getMonth(), by.getOrDefault(d, List.of())));
        return days;
    }
}