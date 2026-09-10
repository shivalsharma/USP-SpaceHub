package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.AlternativeOption;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/bookings")
public class AdminBookingController {
    private final BookingRepository bookings;
    private final AlternativeSuggestionRepository suggestions;
    private final AlternativeService alternativeService;
    private final BookingService bookingService;
    private final CurrentUserService current;
    private final NotificationService notifications;
    private final AuditService audit;

    @GetMapping
    String list(Model m) {
        m.addAttribute("bookings", bookings.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/bookings";
    }

    @PostMapping("/{id}/cancel")
    String cancel(Authentication auth, @PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.cancel(current.require(auth), id);
            ra.addFlashAttribute("success", "Booking cancelled by administrator.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/suggest-move")
    @Transactional
    String suggestMove(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Booking b = bookings.findById(id).orElseThrow();
            if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED)
                throw new IllegalArgumentException("Closed bookings cannot be moved.");
            List<AlternativeOption> opts = alternativeService.findAlternatives(b, 1);
            if (opts.isEmpty())
                throw new IllegalArgumentException("No valid alternative is currently available.");
            AlternativeOption o = opts.getFirst();
            AlternativeSuggestion s = new AlternativeSuggestion();
            s.setBooking(b);
            s.setRoom(o.room());
            s.setDate(o.date());
            s.setStartTime(o.startTime());
            s.setEndTime(o.endTime());
            s.setScore(o.score());
            s.setExplanation("Administrator requested reallocation. " + o.explanation());
            s.setPreserveOriginalOnReject(b.getStatus() == BookingStatus.APPROVED);
            suggestions.save(s);
            if (b.getStatus() != BookingStatus.APPROVED)
                b.setStatus(BookingStatus.ALTERNATIVE_PROPOSED);
            b.setAdminDecisionReason("Administrator proposed a reallocation; awaiting requester decision.");
            bookings.save(b);
            notifications.notify(b.getRequester(), "Booking reallocation proposed",
                    "Suggested move: " + o.room().getCode() + " on " + o.date() + " " + o.startTime() + "-" + o.endTime() + ".",
                    "ADMIN-MOVE-" + b.getId() + "-" + s.getId());
            audit.log("ADMIN_REALLOCATION_PROPOSED", "Booking", b.getId(), s.getExplanation());
            ra.addFlashAttribute("success", "Alternative reallocation sent to requester.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
}