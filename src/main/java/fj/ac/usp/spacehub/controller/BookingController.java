package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.SearchForm;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final CurrentUserService current;
    private final BookingRepository bookings;
    private final AlternativeSuggestionRepository alternatives;
    private final BookingService service;
    private final CourseRepository courses;

    @GetMapping
    String list(Authentication auth, Model m) {
        UserAccount u = current.require(auth);
        m.addAttribute("bookings",
                bookings.findByRequesterIdOrderByDateDescStartTimeDesc(u.getId()));
        m.addAttribute("alternatives",
                alternatives.findByBookingRequesterIdAndStatusOrderByCreatedAtDesc(
                        u.getId(), AlternativeStatus.PROPOSED));
        return "bookings";
    }

    @PostMapping("/submit")
    String submit(
            Authentication auth,
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "0") double score,
            @ModelAttribute SearchForm form,
            RedirectAttributes ra) {

        try {
            /*
             * IMPORTANT:
             * Never trust expectedStudents coming from the browser for
             * LAB/TUTORIAL bookings.
             *
             * The authoritative value is the course roll stored in the
             * Course entity/database.
             */
            if (form.getBookingType() != null
                    && form.getBookingType().isAcademic()) {

                if (form.getCourseId() == null) {
                    throw new IllegalArgumentException(
                            "LAB and TUTORIAL bookings require a course.");
                }

                Course course = courses.findById(form.getCourseId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "The selected course could not be found."));

                if (course.getCourseRoll() < 1) {
                    throw new IllegalArgumentException(
                            "The selected course does not have a valid course roll.");
                }

                // Force academic bookings to use the real course roll.
                form.setExpectedStudents(course.getCourseRoll());
            }

            /*
             * ECA bookings still use the attendance entered by the user.
             */
            if (form.getBookingType() == BookingType.ECA
                    && form.getExpectedStudents() < 1) {
                throw new IllegalArgumentException(
                        "Expected attendance must be at least 1.");
            }

            Booking b = service.submit(
                    current.require(auth),
                    roomId,
                    form,
                    score
            );

            ra.addFlashAttribute(
                    "success",
                    "Booking request #" + b.getId()
                            + " submitted for admin approval.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings";
    }

    @PostMapping("/{id}/cancel")
    String cancel(
            Authentication auth,
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {
            service.cancel(current.require(auth), id);
            ra.addFlashAttribute("success", "Booking cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings";
    }

    @PostMapping("/alternatives/{id}/accept")
    String accept(
            Authentication auth,
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {
            service.acceptAlternative(current.require(auth), id);
            ra.addFlashAttribute(
                    "success",
                    "Alternative accepted and returned to pending approval.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings";
    }

    @PostMapping("/alternatives/{id}/reject")
    String reject(
            Authentication auth,
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {
            service.rejectAlternative(current.require(auth), id);
            ra.addFlashAttribute("success", "Alternative rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings";
    }
}
