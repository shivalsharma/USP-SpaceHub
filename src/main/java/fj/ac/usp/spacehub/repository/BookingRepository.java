package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRequesterIdOrderByDateDescStartTimeDesc(Long userId);
    List<Booking> findByStatusOrderByCreatedAtAsc(BookingStatus status);
    List<Booking> findByStatusInOrderByCreatedAtAsc(Collection<BookingStatus> statuses);
    List<Booking> findByRoomIdAndDateBetweenAndStatus(Long roomId, LocalDate from, LocalDate to, BookingStatus status);
    List<Booking> findByCourseIdAndDateBetweenAndStatusIn(Long courseId, LocalDate from, LocalDate to, Collection<BookingStatus> statuses);
    List<Booking> findByRequesterIdAndDateBetweenAndStatusIn(Long userId, LocalDate from, LocalDate to, Collection<BookingStatus> statuses);
    List<Booking> findByDateBetweenAndStatus(LocalDate from, LocalDate to, BookingStatus status);
    List<Booking> findByDateAndStatus(LocalDate date, BookingStatus status);

    @Query("select b from Booking b where b.room.id=:roomId and b.date=:date and b.status in :statuses and b.startTime < :endTime and b.endTime > :startTime")
    List<Booking> findRoomOverlaps(@Param("roomId") Long roomId, @Param("date") LocalDate date, @Param("startTime") LocalTime start, @Param("endTime") LocalTime end, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("select b from Booking b where b.requester.id=:userId and b.date=:date and b.status in :statuses and b.startTime < :endTime and b.endTime > :startTime")
    List<Booking> findUserOverlaps(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("startTime") LocalTime start, @Param("endTime") LocalTime end, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("select b from Booking b where b.room.id=:roomId and b.date=:date and b.status in :statuses and b.startTime < :endTime and b.endTime > :startTime and (:excludeId is null or b.id<>:excludeId)")
    List<Booking> findRoomOverlapsExcluding(@Param("roomId") Long roomId, @Param("date") LocalDate date, @Param("startTime") LocalTime start, @Param("endTime") LocalTime end, @Param("statuses") Collection<BookingStatus> statuses, @Param("excludeId") Long excludeId);

    @Query("select b from Booking b where b.requester.id=:userId and b.date=:date and b.status in :statuses and b.startTime < :endTime and b.endTime > :startTime and (:excludeId is null or b.id<>:excludeId)")
    List<Booking> findUserOverlapsExcluding(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("startTime") LocalTime start, @Param("endTime") LocalTime end, @Param("statuses") Collection<BookingStatus> statuses, @Param("excludeId") Long excludeId);

    @Query("select b from Booking b where b.requester.id=:userId and b.room.id=:roomId and b.date=:date and b.startTime=:start and b.endTime=:end and b.type=:type and b.status in :statuses and (:courseId is null or b.course.id=:courseId)")
    List<Booking> findDuplicates(@Param("userId") Long userId, @Param("roomId") Long roomId, @Param("courseId") Long courseId, @Param("date") LocalDate date, @Param("start") LocalTime start, @Param("end") LocalTime end, @Param("type") BookingType type, @Param("statuses") Collection<BookingStatus> statuses);
}