package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository users;
    private final RoomRepository rooms;
    private final CourseRepository courses;
    private final BookingRepository bookings;
    private final SettingsService settings;
    private final AuditLogRepository audit;
    private final ReportService reports;

    @GetMapping
    String dashboard(Model m) {
        m.addAttribute("userCount", users.count());
        m.addAttribute("roomCount", rooms.count());
        m.addAttribute("courseCount", courses.count());
        m.addAttribute("pendingCount", bookings.findByStatusOrderByCreatedAtAsc(BookingStatus.PENDING).size());
        return "admin/dashboard";
    }

    @GetMapping("/settings")
    String settings(Model m) { m.addAttribute("settings", settings.all()); return "admin/settings"; }

    @PostMapping("/settings")
    String update(@RequestParam String key, @RequestParam String value, RedirectAttributes ra) {
        try { settings.update(key, value); ra.addFlashAttribute("success", "Setting updated."); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/settings";
    }

    @GetMapping("/audit")
    String audit(Model m) { m.addAttribute("logs", audit.findTop250ByOrderByCreatedAtDesc()); return "admin/audit"; }

    @GetMapping("/reports")
    String report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, Model m) {
        if (to == null) to = LocalDate.now(ZoneId.of("Pacific/Fiji"));
        if (from == null) from = to.minusDays(30);
        m.addAttribute("from", from);
        m.addAttribute("to", to);
        m.addAllAttributes(reports.dashboard(from, to));
        return "admin/reports";
    }

    @GetMapping("/reports/bookings.csv")
    ResponseEntity<String> csv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings-" + from + "-" + to + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(reports.bookingCsv(from, to));
    }
}