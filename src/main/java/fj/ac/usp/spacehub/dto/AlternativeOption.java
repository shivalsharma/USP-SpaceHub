package fj.ac.usp.spacehub.dto;
import fj.ac.usp.spacehub.model.Room;
import java.time.*;

public record AlternativeOption(Room room, LocalDate date, LocalTime startTime, LocalTime endTime,
                                double score, String explanation) {}