package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.BookingStatus;
import fj.ac.usp.spacehub.repository.BookingRepository;
import fj.ac.usp.spacehub.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/approvals")
public class AdminApprovalController {

    private final BookingRepository bookings;
    private final ApprovalService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pending", bookings.findByStatusOrderByCreatedAtAsc(BookingStatus.PENDING));
        return "admin/approvals";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("focus", bookings.findById(id).orElseThrow());
        model.addAttribute("recommendations", service.recommendations(id));
        return "admin/approval-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String reason,
                          RedirectAttributes redirectAttributes) {
        try {
            service.approve(id, reason);
            redirectAttributes.addFlashAttribute("success", "Booking approved and conflicting requests processed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/approvals";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            service.reject(id, reason);
            redirectAttributes.addFlashAttribute("success", "Booking rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/approvals";
    }

    @PostMapping("/{id}/override")
    public String override(@PathVariable Long id,
                           @RequestParam String reason,
                           RedirectAttributes redirectAttributes) {
        try {
            service.approveWithOverride(id, reason);
            redirectAttributes.addFlashAttribute("success", "Booking approved with logged override.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/approvals/" + id;
    }
}