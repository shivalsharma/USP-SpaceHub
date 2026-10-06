package fj.ac.usp.spacehub.service;

import fj.ac.usp.spacehub.dto.CalendarDay;
import fj.ac.usp.spacehub.model.Booking;
import fj.ac.usp.spacehub.model.BookingStatus;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service @RequiredArgsConstructor
public class CalendarService {
    private final BookingRepository bookings;

    public List<CalendarDay> month(YearMonth month, UserAccount user, Long roomId) {
        LocalDate first = month.atDay(1), last = month.atEndOfMonth();
        LocalDate gridStart = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate gridEnd = last.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Booking> all = bookings.findByDateBetweenAndStatusIn(gridStart, gridEnd, List.of(BookingStatus.APPROVED, BookingStatus.REJECTED)).stream()
                // Rejected requests are visible only to their owner or administrators,
                // including when viewing a shared room calendar.
                .filter(b -> b.getStatus() != BookingStatus.REJECTED || (user != null &&
                        (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.IT_ADMIN ||
                                b.getRequester().getId().equals(user.getId()))))
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