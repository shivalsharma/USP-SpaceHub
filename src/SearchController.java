package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.SearchForm;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final CurrentUserService current;
    private final CourseRepository courses;
    private final RoomRepository rooms;
    private final SearchService search;
    private final SettingsService settings;

    @GetMapping
    String page(Authentication auth, Model m) {
        UserAccount user = current.require(auth);
        SearchForm form = defaultForm(user);
        m.addAttribute("searchForm", form);
        addOptions(m, user);
        return "search";
    }

    @PostMapping
    String run(@Valid @ModelAttribute SearchForm searchForm,
               BindingResult br,
               Authentication auth,
               Model m) {
        UserAccount user = current.require(auth);
        addOptions(m, user);

        if (br.hasErrors()) return "search";

        m.addAttribute("result", search.search(user, searchForm));
        return "search";
    }

    private SearchForm defaultForm(UserAccount user) {
        SearchForm f = new SearchForm();
        LocalDate today = LocalDate.now(ZoneId.of("Pacific/Fiji"));
        LocalTime dayStart = settings.getTime("booking.day.start", LocalTime.of(8, 0));
        LocalTime suggestedStart = dayStart.isBefore(LocalTime.of(10, 0)) ? LocalTime.of(10, 0) : dayStart;

        f.setDate(today.plusDays(1));
        f.setStartTime(suggestedStart);
        f.setEndTime(suggestedStart.plusHours(2));
        f.setBookingType(user.getRole() == UserRole.STUDENT ? BookingType.ECA : BookingType.LAB);
        return f;
    }

    private void addOptions(Model m, UserAccount user) {
        List<Course> courseOptions;
        if (user.getRole() == UserRole.LECTURER) {
            courseOptions = courses.findActiveCoursesForLecturer(user.getId());
        } else if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.IT_ADMIN) {
            courseOptions = courses.findByActiveTrueOrderByCode();
        } else {
            courseOptions = List.of();
        }

        List<BookingType> bookingTypes = user.getRole() == UserRole.STUDENT
                ? List.of(BookingType.ECA)
                : List.of(BookingType.LAB, BookingType.TUTORIAL, BookingType.ECA);

        Map<Long, String> courseFacilities = courseOptions.stream()
                .collect(Collectors.toMap(
                        Course::getId,
                        c -> c.getRequiredFacilities() == null ? "" : c.getRequiredFacilities().stream()
                                .map(FacilityUtil::normalize)
                                .sorted()
                                .collect(Collectors.joining(", ")),
                        (a, b) -> a,
                        LinkedHashMap::new));

        LocalDate today = LocalDate.now(ZoneId.of("Pacific/Fiji"));
        int horizon = settings.getInt("booking.horizon.days", 120);

        m.addAttribute("courses", courseOptions);
        m.addAttribute("courseFacilities", courseFacilities);
        m.addAttribute("bookingTypes", bookingTypes);
        m.addAttribute("facilityOptions", FacilityUtil.standardOptions());
        m.addAttribute("buildings", rooms.findAllByOrderByCode().stream()
                .map(Room::getBuilding)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList());
        m.addAttribute("today", today);
        m.addAttribute("maxBookingDate", today.plusDays(horizon));
        m.addAttribute("bookingDayStart", settings.getTime("booking.day.start", LocalTime.of(8, 0)));
        m.addAttribute("bookingDayEnd", settings.getTime("booking.day.end", LocalTime.of(22, 0)));
        m.addAttribute("maxDurationHours", settings.getInt("booking.max.duration.hours", 4));
        m.addAttribute("isStudent", user.getRole() == UserRole.STUDENT);
    }

}
