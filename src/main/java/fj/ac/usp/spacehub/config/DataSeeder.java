package fj.ac.usp.spacehub.config;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Component @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users; private final RoomRepository rooms; private final CourseRepository courses; private final BookingRepository bookings; private final ScheduleEntryRepository schedules; private final SystemSettingRepository settings; private final PasswordEncoder encoder;
    @Override @Transactional public void run(String... args){ seedSettings(); if(users.count()==0) seedDemo(); }
    private void seedSettings(){
        put("booking.day.start","08:00","Earliest booking start time"); put("booking.day.end","22:00","Latest booking end time"); put("booking.horizon.days","120","How many days ahead users may book"); put("booking.max.duration.hours","4","Maximum consecutive booking duration in hours");
        put("prime.time.start","08:00","Prime-time start"); put("prime.time.end","12:00","Prime-time end"); put("course.default.max.weekly.bookings","4","Default weekly booking quota for newly created courses"); put("course.default.prime.time.allowance","1","Default weekly prime-time allowance for newly created courses"); put("lecturer.max.daily.bookings","3","Default maximum active booking requests per day"); put("lecturer.max.weekly.bookings","10","Default maximum active booking requests per week"); put("search.result.count","8","Maximum optimized search results");
        put("score.weight.facility","25","Search weight: facility suitability"); put("score.weight.capacity","20","Search weight: capacity efficiency"); put("score.weight.timeFairness","20","Search weight: time fairness"); put("score.weight.courseNeed","15","Search weight: course booking need"); put("score.weight.utilization","10","Search weight: room utilization balance"); put("score.weight.roomFairness","10","Search weight: room fairness");
        put("reminder.hours.before","24","Reminder lead time in hours"); put("admin.override.enabled","true","Allow quota-only administrative overrides with a mandatory reason");
    }
    private void put(String k,String v,String d){ if(!settings.existsById(k)) settings.save(new SystemSetting(k,v,d)); }
    private UserAccount user(String code,String name,String email,String pw,UserRole role,String dept){ UserAccount u=new UserAccount();u.setUserCode(code);u.setName(name);u.setEmail(email);u.setPasswordHash(encoder.encode(pw));u.setRole(role);u.setDepartment(dept);return users.save(u); }
    private Room room(String code,String name,String building,int cap,int pcs,boolean proj,boolean wb,AccessLevel access){ Room r=new Room();r.setCode(code);r.setName(name);r.setBuilding(building);r.setCapacity(cap);r.setComputerCount(pcs);r.setOperationalComputers(pcs);r.setProjectorAvailable(proj);r.setProjectorOperational(proj);r.setWhiteboardAvailable(wb);r.setWhiteboardOperational(wb);r.setAccessLevel(access);r.setStatus(RoomStatus.ACTIVE);r.setFacilities(new LinkedHashSet<>(Set.of("AIR_CONDITIONING")));return rooms.save(r); }
    private void seedDemo(){
        UserAccount admin=user("ADM001","System Administrator","admin@usp.ac.fj","Admin@123",UserRole.ADMIN,"SITEMP");
        user("IT001","IT Administrator","itadmin@usp.ac.fj","ITAdmin@123",UserRole.IT_ADMIN,"ITS");
        UserAccount lec1=user("STAFF001","Litia Kumar","lecturer1@usp.ac.fj","Lecturer@123",UserRole.LECTURER,"SITEMP");
        UserAccount lec2=user("STAFF002","Jone Ratu","lecturer2@usp.ac.fj","Lecturer@123",UserRole.LECTURER,"SITEMP");
        UserAccount student=user("S12345678","Ana Singh","student1@usp.ac.fj","Student@123",UserRole.STUDENT,"SITEMP");
        Room lab3=room("ICT-LAB-3","ICT Lab 3","ICT Building",45,45,true,true,AccessLevel.BOTH);
        Room lab2=room("ICT-LAB-2","ICT Lab 2","ICT Building",60,60,true,true,AccessLevel.BOTH);
        Room main=room("MAIN-LAB","Main Computer Lab","ICT Building",100,100,true,true,AccessLevel.LECTURER_ONLY);
        Room tut=room("TUT-021","Tutorial Room 021","Engineering Building",30,0,true,true,AccessLevel.BOTH);
        Room eca=room("ROOM-006","Multipurpose Room 006","Student Centre",50,0,true,true,AccessLevel.BOTH);
        lab3.getFacilities().add("SPECIALIZED_SOFTWARE"); rooms.save(lab3);
        Course cs214=new Course(); cs214.setCode("CS214"); cs214.setName("Software Engineering"); cs214.setCourseRoll(42); cs214.setRequiredLabSessions(3); cs214.setRequiredTutorialSessions(1); cs214.setMaxWeeklyBookings(4); cs214.setPrimeTimeAllowance(2); cs214.setRequiredFacilities(new LinkedHashSet<>(Set.of("COMPUTER","PROJECTOR"))); cs214.setLecturers(new LinkedHashSet<>(Set.of(lec1))); courses.save(cs214);
        Course cs315=new Course(); cs315.setCode("CS315"); cs315.setName("Information Systems"); cs315.setCourseRoll(25); cs315.setRequiredLabSessions(1); cs315.setRequiredTutorialSessions(3); cs315.setMaxWeeklyBookings(4); cs315.setPrimeTimeAllowance(2); cs315.setRequiredFacilities(new LinkedHashSet<>(Set.of("PROJECTOR","WHITEBOARD"))); cs315.setLecturers(new LinkedHashSet<>(Set.of(lec2))); courses.save(cs315);
        LocalDate demo=LocalDate.now(ZoneId.of("Pacific/Fiji")).plusDays(3); while(demo.getDayOfWeek()==DayOfWeek.SUNDAY) demo=demo.plusDays(1);
        createPending(lec1,cs214,lab3,BookingType.LAB,demo,LocalTime.of(14,0),LocalTime.of(16,0),42,"CS214 lab practical",Set.of("COMPUTER","PROJECTOR"));
        createPending(lec2,cs315,lab3,BookingType.TUTORIAL,demo,LocalTime.of(14,0),LocalTime.of(16,0),25,"CS315 tutorial",Set.of("PROJECTOR","WHITEBOARD"));
        createPending(student,null,lab3,BookingType.ECA,demo,LocalTime.of(14,0),LocalTime.of(16,0),30,"Computing Club workshop",Set.of("PROJECTOR"));
        ScheduleEntry s=new ScheduleEntry();s.setUser(lec1);s.setDate(demo.plusDays(1));s.setStartTime(LocalTime.of(10,0));s.setEndTime(LocalTime.of(12,0));s.setTitle("Existing CS214 lecture");schedules.save(s);
    }
    private void createPending(UserAccount u,Course c,Room r,BookingType t,LocalDate d,LocalTime s,LocalTime e,int students,String purpose,Set<String> req){Booking b=new Booking();b.setRequester(u);b.setCourse(c);b.setRoom(r);b.setType(t);b.setDate(d);b.setStartTime(s);b.setEndTime(e);b.setExpectedStudents(students);b.setPurpose(purpose);b.setRequiredFacilities(new LinkedHashSet<>(req));b.setStatus(BookingStatus.PENDING);b.setSearchScore(85.0);b.setRecommendationReason("Seeded demonstration conflict request");bookings.save(b);}
}
