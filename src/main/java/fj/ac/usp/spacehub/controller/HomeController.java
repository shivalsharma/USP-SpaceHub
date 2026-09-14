package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final CurrentUserService current;
    private final BookingRepository bookings;
    private final NotificationRepository notifications;
    private final CourseRepository courses;

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping({"/", "/dashboard"})
    String dashboard(Authentication auth, Model m) {
        UserAccount u = current.require(auth);
        m.addAttribute("user", u);
        List<Booking> mine = bookings.findByRequesterIdOrderByDateDescStartTimeDesc(u.getId());
        m.addAttribute("pending", mine.stream().filter(b -> b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.ALTERNATIVE_PROPOSED).count());
        m.addAttribute("approved", mine.stream().filter(b -> b.getStatus() == BookingStatus.APPROVED).count());
        m.addAttribute("upcoming", mine.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED && !b.getDate().isBefore(LocalDate.now(ZoneId.of("Pacific/Fiji"))))
                .sorted(Comparator.comparing(Booking::getDate).thenComparing(Booking::getStartTime))
                .limit(5).toList());
        m.addAttribute("unread", notifications.countByUserIdAndReadFlagFalse(u.getId()));
        m.addAttribute("courses", u.getRole() == UserRole.LECTURER ? courses.findActiveCoursesForLecturer(u.getId()) : List.of());
        return "dashboard";
    }
}