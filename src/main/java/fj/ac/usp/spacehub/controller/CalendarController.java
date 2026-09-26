package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.repository.RoomRepository;
import fj.ac.usp.spacehub.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@Controller
@RequiredArgsConstructor
public class CalendarController {
    private final CurrentUserService current;
    private final CalendarService calendar;
    private final RoomRepository rooms;

    @GetMapping("/calendar")
    String page(Authentication auth, @RequestParam(required = false) String month, @RequestParam(required = false) Long roomId, Model m) {
        UserAccount u = current.require(auth);
        YearMonth ym;
        try {
            ym = month == null ? YearMonth.now(ZoneId.of("Pacific/Fiji")) : YearMonth.parse(month);
        } catch (Exception e) {
            ym = YearMonth.now(ZoneId.of("Pacific/Fiji"));
        }
        m.addAttribute("month", ym);
        m.addAttribute("prev", ym.minusMonths(1));
        m.addAttribute("next", ym.plusMonths(1));
        m.addAttribute("days", calendar.month(ym, u, roomId));
        m.addAttribute("rooms", rooms.findAllByOrderByCode());
        m.addAttribute("roomId", roomId);
        return "calendar";
    }
}