package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.ScheduleForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {
    private final CurrentUserService current;
    private final ScheduleEntryRepository schedules;
    private final CourseRepository courses;
    private final AuditService audit;

    @GetMapping
    String page(Authentication auth, Model m) {
        UserAccount u = current.require(auth);
        m.addAttribute("user", u);
        m.addAttribute("entries", schedules.findByUserIdOrderByDateAscStartTimeAsc(u.getId()));
        m.addAttribute("scheduleForm", new ScheduleForm());
        m.addAttribute("courses", u.getRole() == UserRole.LECTURER ? courses.findActiveCoursesForLecturer(u.getId()) : List.of());
        return "profile";
    }

    @PostMapping("/schedule")
    String add(Authentication auth, @Valid @ModelAttribute ScheduleForm scheduleForm, BindingResult br, RedirectAttributes ra) {
        UserAccount u = current.require(auth);
        if (br.hasErrors() || !scheduleForm.getEndTime().isAfter(scheduleForm.getStartTime())) {
            ra.addFlashAttribute("error", "Enter a valid schedule date/time.");
            return "redirect:/profile";
        }
        ScheduleEntry s = new ScheduleEntry();
        s.setUser(u);
        s.setDate(scheduleForm.getDate());
        s.setStartTime(scheduleForm.getStartTime());
        s.setEndTime(scheduleForm.getEndTime());
        s.setTitle(scheduleForm.getTitle());
        schedules.save(s);
        audit.log("TIMETABLE_ENTRY_CREATED", "ScheduleEntry", s.getId(), s.getTitle());
        ra.addFlashAttribute("success", "Busy/timetable entry added.");
        return "redirect:/profile";
    }

    @PostMapping("/schedule/{id}/delete")
    String del(Authentication auth, @PathVariable Long id, RedirectAttributes ra) {
        UserAccount u = current.require(auth);
        ScheduleEntry s = schedules.findById(id).orElseThrow();
        if (!s.getUser().getId().equals(u.getId())) throw new IllegalArgumentException("Not your schedule entry");
        schedules.delete(s);
        audit.log("TIMETABLE_ENTRY_DELETED", "ScheduleEntry", id, s.getTitle());
        ra.addFlashAttribute("success", "Schedule entry removed.");
        return "redirect:/profile";
    }
}