package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.ScheduleForm;
import fj.ac.usp.spacehub.model.Course;
import fj.ac.usp.spacehub.model.ScheduleEntry;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.model.UserRole;
import fj.ac.usp.spacehub.repository.CourseRepository;
import fj.ac.usp.spacehub.repository.ScheduleEntryRepository;
import fj.ac.usp.spacehub.service.AuditService;
import fj.ac.usp.spacehub.service.CurrentUserService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    /* =========================================================
       PROFILE PAGE
       GET /profile
       ========================================================= */

    @GetMapping
    String page(
            Authentication auth,
            Model model
    ) {

        UserAccount user = current.require(auth);


        model.addAttribute(
                "user",
                user
        );


        model.addAttribute(
                "entries",
                schedules.findByUserIdOrderByDateAscStartTimeAsc(
                        user.getId()
                )
        );


        model.addAttribute(
                "scheduleForm",
                new ScheduleForm()
        );


        List<Course> assignedCourses;

        if (user.getRole() == UserRole.LECTURER) {

            assignedCourses =
                    courses.findActiveCoursesForLecturer(
                            user.getId()
                    );

        } else {

            assignedCourses = List.of();

        }


        model.addAttribute(
                "courses",
                assignedCourses
        );


        return "profile";
    }



    /* =========================================================
       EDIT PROFILE PAGE
       GET /profile/edit
       ========================================================= */

    @GetMapping("/edit")
    String editProfile(
            Authentication auth,
            Model model
    ) {

        UserAccount user = current.require(auth);


        model.addAttribute(
                "user",
                user
        );


        return "edit-profile";
    }



    /* =========================================================
       UPDATE PROFILE
       POST /profile/update
       ========================================================= */

    @PostMapping("/update")
    @Transactional
    String updateProfile(
            Authentication auth,

            @RequestParam String name,

            @RequestParam(required = false)
            String department,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user = current.require(auth);


        /* -----------------------------------------------------
           Validate name
           ----------------------------------------------------- */

        if (
                name == null
                ||
                name.trim().isEmpty()
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Full name cannot be empty."
            );


            return "redirect:/profile/edit";
        }


        /* -----------------------------------------------------
           Update editable profile details
           ----------------------------------------------------- */

        user.setName(
                name.trim()
        );


        if (department != null) {

            user.setDepartment(
                    department.trim()
            );

        }


        /*
         * The UserAccount returned by CurrentUserService is
         * updated inside this transaction.
         *
         * Hibernate/JPA will persist the changes when the
         * transaction completes.
         */


        /* -----------------------------------------------------
           Audit log
           ----------------------------------------------------- */

        audit.log(
                "PROFILE_UPDATED",
                "UserAccount",
                user.getId(),
                "Profile details updated"
        );


        /* -----------------------------------------------------
           Success message
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully."
        );


        return "redirect:/profile";
    }



    /* =========================================================
       ADD TIMETABLE / BUSY TIME
       POST /profile/schedule
       ========================================================= */

    @PostMapping("/schedule")
    String add(
            Authentication auth,

            @Valid
            @ModelAttribute
            ScheduleForm scheduleForm,

            BindingResult bindingResult,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user = current.require(auth);


        /* -----------------------------------------------------
           Validate schedule
           ----------------------------------------------------- */

        if (
                bindingResult.hasErrors()
                ||
                scheduleForm.getStartTime() == null
                ||
                scheduleForm.getEndTime() == null
                ||
                !scheduleForm
                        .getEndTime()
                        .isAfter(
                                scheduleForm.getStartTime()
                        )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Enter a valid schedule date/time."
            );


            return "redirect:/profile";
        }


        /* -----------------------------------------------------
           Create schedule entry
           ----------------------------------------------------- */

        ScheduleEntry scheduleEntry =
                new ScheduleEntry();


        scheduleEntry.setUser(
                user
        );


        scheduleEntry.setDate(
                scheduleForm.getDate()
        );


        scheduleEntry.setStartTime(
                scheduleForm.getStartTime()
        );


        scheduleEntry.setEndTime(
                scheduleForm.getEndTime()
        );


        scheduleEntry.setTitle(
                scheduleForm.getTitle()
        );


        /* -----------------------------------------------------
           Save schedule
           ----------------------------------------------------- */

        schedules.save(
                scheduleEntry
        );


        /* -----------------------------------------------------
           Audit log
           ----------------------------------------------------- */

        audit.log(
                "TIMETABLE_ENTRY_CREATED",
                "ScheduleEntry",
                scheduleEntry.getId(),
                scheduleEntry.getTitle()
        );


        /* -----------------------------------------------------
           Success message
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Busy/timetable entry added."
        );


        return "redirect:/profile";
    }



    /* =========================================================
       DELETE TIMETABLE / BUSY TIME
       POST /profile/schedule/{id}/delete
       ========================================================= */

    @PostMapping("/schedule/{id}/delete")
    String deleteSchedule(
            Authentication auth,

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user = current.require(auth);


        /* -----------------------------------------------------
           Find schedule entry
           ----------------------------------------------------- */

        ScheduleEntry scheduleEntry =
                schedules
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Schedule entry not found."
                                        )
                        );


        /* -----------------------------------------------------
           Security check
           ----------------------------------------------------- */

        if (
                !scheduleEntry
                        .getUser()
                        .getId()
                        .equals(
                                user.getId()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Not your schedule entry."
            );

        }


        String scheduleTitle =
                scheduleEntry.getTitle();


        /* -----------------------------------------------------
           Delete
           ----------------------------------------------------- */

        schedules.delete(
                scheduleEntry
        );


        /* -----------------------------------------------------
           Audit log
           ----------------------------------------------------- */

        audit.log(
                "TIMETABLE_ENTRY_DELETED",
                "ScheduleEntry",
                id,
                scheduleTitle
        );


        /* -----------------------------------------------------
           Success message
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Schedule entry removed."
        );


        return "redirect:/profile";
    }

}