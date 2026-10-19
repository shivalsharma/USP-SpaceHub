package fj.ac.usp.spacehub.dto;

import fj.ac.usp.spacehub.model.Booking;
import fj.ac.usp.spacehub.model.ScheduleEntry;

import java.time.LocalTime;

/**
 * One item shown inside a calendar day: either an approved room booking or a
 * busy/timetable entry the user added on their profile page.
 * Exactly one of {@code booking} / {@code schedule} is set.
 */
public record CalendarEntry(LocalTime startTime,
                            LocalTime endTime,
                            Booking booking,
                            ScheduleEntry schedule,
                            boolean clash) {

    public static CalendarEntry of(Booking b) {
        return new CalendarEntry(b.getStartTime(), b.getEndTime(), b, null, false);
    }

    /** @param clash true when this busy time overlaps one of the user's own approved bookings. */
    public static CalendarEntry of(ScheduleEntry s, boolean clash) {
        return new CalendarEntry(s.getStartTime(), s.getEndTime(), null, s, clash);
    }

    /** True for a busy/timetable entry from the profile page. */
    public boolean isBusy() {
        return schedule != null;
    }
}
