package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;
public interface MaintenanceRepository extends JpaRepository<MaintenanceWindow,Long>{
    @Query("select m from MaintenanceWindow m where m.room.id=:roomId and m.date=:date and m.startTime < :endTime and m.endTime > :startTime")
    List<MaintenanceWindow> findOverlaps(@Param("roomId")Long roomId,@Param("date")LocalDate date,@Param("startTime")LocalTime start,@Param("endTime")LocalTime end);
    List<MaintenanceWindow> findByRoomIdOrderByDateDescStartTimeAsc(Long roomId);
}
