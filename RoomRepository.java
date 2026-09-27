package fj.ac.usp.spacehub.repository;

import fj.ac.usp.spacehub.model.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCodeIgnoreCase(String code);
    List<Room> findByStatusOrderByCode(RoomStatus status);
    List<Room> findAllByOrderByCode();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id=:id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
