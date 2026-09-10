package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.CourseForm;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class AdminCourseController {
    private final CourseRepository courses;
    private final UserRepository users;
    private final AuditService audit;
    private final SettingsService settings;

    @GetMapping
    String list(Model m) { m.addAttribute("courses", courses.findAll()); return "admin/courses"; }

    @GetMapping("/new")
    String create(Model m) {
        CourseForm f = new CourseForm();
        f.setMaxWeeklyBookings(settings.getInt("course.default.max.weekly.bookings", 4));
        f.setPrimeTimeAllowance(settings.getInt("course.default.prime.time.allowance", 1));
        m.addAttribute("form", f);
        options(m);
        return "admin/course-form";
    }

    @GetMapping("/{id}/edit")
    String edit(@PathVariable Long id, Model m) {
        Course c = courses.findById(id).orElseThrow();
        CourseForm f = new CourseForm();
        f.setId(c.getId());
        f.setCode(c.getCode());
        f.setName(c.getName());
        f.setCourseRoll(c.getCourseRoll());
        f.setRequiredLabSessions(c.getRequiredLabSessions());
        f.setRequiredTutorialSessions(c.getRequiredTutorialSessions());
        f.setMaxWeeklyBookings(c.getMaxWeeklyBookings());
        f.setPrimeTimeAllowance(c.getPrimeTimeAllowance());
        f.setActive(c.isActive());
        f.setRequiredFacilitiesCsv(FacilityUtil.toCsv(c.getRequiredFacilities()));
        f.setLecturerIds(c.getLecturers().stream().map(UserAccount::getId).toList());
        m.addAttribute("form", f);
        options(m);
        return "admin/course-form";
    }

    @PostMapping("/save")
    String save(@Valid @ModelAttribute("form") CourseForm f, BindingResult br, Model m, RedirectAttributes ra) {
        courses.findByCodeIgnoreCase(f.getCode()).filter(x -> !Objects.equals(x.getId(), f.getId()))
                .ifPresent(x -> br.rejectValue("code", "duplicate", "Course code is already in use"));
        if (br.hasErrors()) { options(m); return "admin/course-form"; }
        Course c = f.getId() == null ? new Course() : courses.findById(f.getId()).orElseThrow();
        c.setCode(f.getCode().trim().toUpperCase());
        c.setName(f.getName().trim());
        c.setCourseRoll(f.getCourseRoll());
        c.setRequiredLabSessions(f.getRequiredLabSessions());
        c.setRequiredTutorialSessions(f.getRequiredTutorialSessions());
        c.setMaxWeeklyBookings(f.getMaxWeeklyBookings());
        c.setPrimeTimeAllowance(f.getPrimeTimeAllowance());
        c.setActive(f.isActive());
        c.setRequiredFacilities(FacilityUtil.parseCsv(f.getRequiredFacilitiesCsv()));
        Set<UserAccount> ls = new LinkedHashSet<>();
        if (f.getLecturerIds() != null)
            for (Long id : f.getLecturerIds())
                users.findById(id).filter(u -> u.getRole() == UserRole.LECTURER).ifPresent(ls::add);
        c.setLecturers(ls);
        courses.save(c);
        audit.log(f.getId() == null ? "COURSE_CREATED" : "COURSE_UPDATED", "Course", c.getId(), c.getCode() + " roll=" + c.getCourseRoll());
        ra.addFlashAttribute("success", "Course saved.");
        return "redirect:/admin/courses";
    }

    private void options(Model m) {
        m.addAttribute("lecturers", users.findByRoleAndActiveTrueOrderByName(UserRole.LECTURER));
        m.addAttribute("facilityOptions", FacilityUtil.standardOptions());
    }
}