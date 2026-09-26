package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry,Long>{
    List<ScheduleEntry> findByUserIdOrderByDateAscStartTimeAsc(Long userId);
    @Query("select s from ScheduleEntry s where s.user.id=:userId and s.date=:date and s.startTime < :endTime and s.endTime > :startTime")
    List<ScheduleEntry> findOverlaps(@Param("userId")Long userId,@Param("date")LocalDate date,@Param("startTime")LocalTime start,@Param("endTime")LocalTime end);
}
