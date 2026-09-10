package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.*;
import java.time.*;
import java.util.Set;

public record BookingCandidate(
        UserAccount requester,
        Course course,
        Room room,
        BookingType type,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int expectedStudents,
        Set<String> requiredFacilities,
        Long excludeBookingId
) {}