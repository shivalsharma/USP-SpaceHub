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
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final CurrentUserService current;
    private final CourseRepository courses;
    private final RoomRepository rooms;
    private final SearchService search;

    @GetMapping
    String page(Authentication auth, Model m) {
        UserAccount u = current.require(auth);
        SearchForm f = new SearchForm();
        f.setDate(LocalDate.now(ZoneId.of("Pacific/Fiji")).plusDays(1));
        f.setStartTime(LocalTime.of(10, 0));
        f.setEndTime(LocalTime.of(12, 0));
        f.setBookingType(u.getRole() == UserRole.STUDENT ? BookingType.ECA : BookingType.LAB);
        m.addAttribute("searchForm", f);
        addOptions(m, u);
        return "search";
    }

    @PostMapping
    String run(@Valid @ModelAttribute SearchForm searchForm, BindingResult br, Authentication auth, Model m) {
        UserAccount u = current.require(auth);
        addOptions(m, u);
        if (br.hasErrors()) return "search";
        m.addAttribute("result", search.search(u, searchForm));
        return "search";
    }

    private void addOptions(Model m, UserAccount u) {
        m.addAttribute("courses", u.getRole() == UserRole.LECTURER ? courses.findActiveCoursesForLecturer(u.getId()) : List.of());
        m.addAttribute("bookingTypes", u.getRole() == UserRole.STUDENT ? List.of(BookingType.ECA) : List.of(BookingType.LAB, BookingType.TUTORIAL, BookingType.ECA));
        m.addAttribute("facilityOptions", FacilityUtil.standardOptions());
        m.addAttribute("buildings", rooms.findAllByOrderByCode().stream().map(Room::getBuilding).filter(Objects::nonNull).distinct().sorted().toList());
    }
}