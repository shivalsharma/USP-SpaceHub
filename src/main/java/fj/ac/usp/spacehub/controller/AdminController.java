package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.BookingStatus;
import fj.ac.usp.spacehub.repository.AuditLogRepository;
import fj.ac.usp.spacehub.repository.BookingRepository;
import fj.ac.usp.spacehub.repository.CourseRepository;
import fj.ac.usp.spacehub.repository.RoomRepository;
import fj.ac.usp.spacehub.repository.UserRepository;
import fj.ac.usp.spacehub.service.ReportService;
import fj.ac.usp.spacehub.service.SettingsService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;


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



    /* =========================================================
       ADMIN DASHBOARD
       ========================================================= */
    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute(
                "userCount",
                users.count()
        );

        model.addAttribute(
                "roomCount",
                rooms.count()
        );

        model.addAttribute(
                "courseCount",
                courses.count()
        );

        model.addAttribute(
                "pendingCount",
                bookings
                        .findByStatusOrderByCreatedAtAsc(
                                BookingStatus.PENDING
                        )
                        .size()
        );

        return "admin/dashboard";
    }



    /* =========================================================
       SETTINGS PAGE
       ========================================================= */
    @GetMapping("/settings")
    public String settings(Model model) {

        model.addAttribute(
                "settings",
                settings.all()
        );

        return "admin/settings";
    }



    /* =========================================================
       UPDATE SETTINGS
       ========================================================= */
    @PostMapping("/settings")
    public String updateSetting(
            @RequestParam String key,
            @RequestParam String value,
            RedirectAttributes redirectAttributes) {

        try {

            settings.update(
                    key,
                    value
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Setting updated."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        }

        return "redirect:/admin/settings";
    }



    /* =========================================================
       AUDIT LOG
       ========================================================= */
    @GetMapping("/audit")
    public String audit(Model model) {

        model.addAttribute(
                "logs",
                audit.findTop250ByOrderByCreatedAtDesc()
        );

        return "admin/audit";
    }



    /* =========================================================
       REPORTS PAGE
       ========================================================= */
    @GetMapping("/reports")
    public String report(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to,

            Model model) {


        if (to == null) {

            to = LocalDate.now(
                    ZoneId.of("Pacific/Fiji")
            );

        }


        if (from == null) {

            from = to.minusDays(30);

        }


        model.addAttribute(
                "from",
                from
        );

        model.addAttribute(
                "to",
                to
        );


        model.addAllAttributes(
                reports.dashboard(
                        from,
                        to
                )
        );


        return "admin/reports";
    }



    /* =========================================================
       EXPORT BOOKINGS REPORT CSV
       ========================================================= */
    @GetMapping("/reports/bookings.csv")
    public ResponseEntity<String> csv(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to) {


        return ResponseEntity
                .ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=bookings-"
                                + from
                                + "-"
                                + to
                                + ".csv"
                )

                .contentType(
                        MediaType.parseMediaType(
                                "text/csv"
                        )
                )

                .body(
                        reports.bookingCsv(
                                from,
                                to
                        )
                );
    }

}