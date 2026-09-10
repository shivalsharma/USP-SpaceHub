package fj.ac.usp.spacehub.repository;
import fj.ac.usp.spacehub.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface CourseRepository extends JpaRepository<Course,Long>{
    Optional<Course> findByCodeIgnoreCase(String code);
    List<Course> findByActiveTrueOrderByCode();
    @Query("select distinct c from Course c join c.lecturers l where l.id=:userId and c.active=true order by c.code")
    List<Course> findActiveCoursesForLecturer(@Param("userId") Long userId);
}
