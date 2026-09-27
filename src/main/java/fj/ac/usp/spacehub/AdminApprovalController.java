package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.Booking;
import fj.ac.usp.spacehub.model.BookingStatus;
import fj.ac.usp.spacehub.repository.BookingRepository;
import fj.ac.usp.spacehub.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/approvals")
public class AdminApprovalController {

    private final BookingRepository bookings;
    private final ApprovalService service;

    @GetMapping
    public String list(Model model) {
        try {
            List<Booking> pendingBookings =
                    bookings.findByStatusOrderByCreatedAtAsc(BookingStatus.PENDING);

            model.addAttribute("pending", pendingBookings);
        } catch (Exception e) {
            model.addAttribute("pending", List.of());
            model.addAttribute(
                    "approvalLoadError",
                    e.getClass().getSimpleName()
                            + ": "
                            + (e.getMessage() == null
                                ? "Unable to load pending bookings."
                                : e.getMessage())
            );
        }

        return "admin/approvals";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            Booking focus = bookings.findById(id)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Booking request #" + id + " was not found."
                            ));

            model.addAttribute("focus", focus);

            var recommendations = service.recommendations(id);
            model.addAttribute("recommendations", recommendations);

        } catch (Exception e) {
            model.addAttribute("recommendations", List.of());
            model.addAttribute(
                    "reviewError",
                    e.getClass().getSimpleName()
                            + ": "
                            + (e.getMessage() == null
                                ? "Unable to calculate the booking recommendation."
                                : e.getMessage())
            );
        }

        return "admin/approval-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {

        try {
            service.approve(id, reason);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Booking approved and conflicting requests processed."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/approvals";
    }

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {

        try {
            service.reject(id, reason);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Booking rejected."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/approvals";
    }

    @PostMapping("/{id}/override")
    public String override(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {

        try {
            service.approveWithOverride(id, reason);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Booking approved with logged override."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/approvals/" + id;
    }

    @PostMapping("/{id}/suggest-alternative")
    public String suggestAlternative(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            service.suggestAlternative(id);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Alternative booking suggestion sent to the requester."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/approvals/" + id;
    }
}
