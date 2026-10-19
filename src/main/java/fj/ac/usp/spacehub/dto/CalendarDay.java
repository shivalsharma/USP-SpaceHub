package fj.ac.usp.spacehub.dto;
import java.time.LocalDate;
import java.util.List;

/** A day cell in the month grid. Entries are bookings and busy times, sorted by start time. */
public record CalendarDay(LocalDate date, boolean inMonth, boolean today, List<CalendarEntry> entries) {}
