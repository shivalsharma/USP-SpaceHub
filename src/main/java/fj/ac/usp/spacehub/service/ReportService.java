package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor
public class ReportService {
    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final CourseRepository courses;
    private final SettingsService settings;

    public Map<String, Object> dashboard(LocalDate from, LocalDate to) {
        List<Booking> approved = bookings.findByDateBetweenAndStatus(from, to, BookingStatus.APPROVED);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("approvedCount", approved.size());
        out.put("pendingCount", bookings.findByStatusInOrderByCreatedAtAsc(List.of(BookingStatus.PENDING, BookingStatus.ALTERNATIVE_PROPOSED)).size());
        Map<BookingType, Long> byType = new EnumMap<>(BookingType.class);
        for (BookingType t : BookingType.values()) byType.put(t, approved.stream().filter(b -> b.getType() == t).count());
        out.put("byType", byType);
        List<Map<String, Object>> roomRows = new ArrayList<>();
        double dailyHours = ChronoUnit.MINUTES.between(
                settings.getTime("booking.day.start", LocalTime.of(8, 0)),
                settings.getTime("booking.day.end", LocalTime.of(22, 0))) / 60.0;
        long days = Math.max(1, ChronoUnit.DAYS.between(from, to) + 1);
        double available = dailyHours * days;
        for (Room r : rooms.findAllByOrderByCode()) {
            double used = approved.stream().filter(b -> b.getRoom().getId().equals(r.getId()))
                    .mapToDouble(b -> ChronoUnit.MINUTES.between(b.getStartTime(), b.getEndTime()) / 60.0).sum();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", r.getCode());
            row.put("name", r.getName());
            row.put("hours", Math.round(used * 10.0) / 10.0);
            row.put("utilization", Math.round((used / Math.max(1, available) * 100) * 10.0) / 10.0);
            row.put("bookings", approved.stream().filter(b -> b.getRoom().getId().equals(r.getId())).count());
            roomRows.add(row);
        }
        roomRows.sort(Comparator.comparingDouble(x -> -((Number) x.get("hours")).doubleValue()));
        out.put("rooms", roomRows);
        List<Map<String, Object>> courseRows = new ArrayList<>();
        for (Course c : courses.findAll()) {
            long n = approved.stream().filter(b -> b.getCourse() != null && b.getCourse().getId().equals(c.getId())).count();
            if (n > 0) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("code", c.getCode());
                row.put("name", c.getName());
                row.put("bookings", n);
                courseRows.add(row);
            }
        }
        courseRows.sort(Comparator.comparingLong(x -> -((Number) x.get("bookings")).longValue()));
        out.put("courses", courseRows);
        return out;
    }

    public String bookingCsv(LocalDate from, LocalDate to) {
        StringBuilder s = new StringBuilder("id,status,type,date,start,end,room,course,requester,students,purpose\n");
        for (Booking b : bookings.findByDateBetweenAndStatus(from, to, BookingStatus.APPROVED)) {
            s.append(b.getId()).append(',').append(b.getStatus()).append(',').append(b.getType()).append(',')
                    .append(b.getDate()).append(',').append(b.getStartTime()).append(',').append(b.getEndTime()).append(',')
                    .append(csv(b.getRoom().getCode())).append(',').append(csv(b.getCourse() == null ? "" : b.getCourse().getCode())).append(',')
                    .append(csv(b.getRequester().getEmail())).append(',').append(b.getExpectedStudents()).append(',')
                    .append(csv(b.getPurpose())).append('\n');
        }
        return s.toString();
    }

    private String csv(String v) { return '"' + v.replace("\"", "\"\"") + '"'; }
}