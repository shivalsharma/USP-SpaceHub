package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.Booking;
import java.time.LocalDate;
import java.util.List;

public record CalendarDay(LocalDate date, boolean inMonth, List<Booking> bookings) {}