package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.NotificationRepository;
import fj.ac.usp.spacehub.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final CurrentUserService current;
    private final NotificationRepository repo;

    @GetMapping
    String page(Authentication auth, Model m) {
        UserAccount u = current.require(auth);
        m.addAttribute("notifications", repo.findTop30ByUserIdOrderByCreatedAtDesc(u.getId()));
        return "notifications";
    }

    @PostMapping("/{id}/read")
    String read(Authentication auth, @PathVariable Long id) {
        UserAccount u = current.require(auth);
        Notification n = repo.findById(id).orElseThrow();
        if (n.getUser().getId().equals(u.getId())) {
            n.setReadFlag(true);
            repo.save(n);
        }
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    String all(Authentication auth) {
        UserAccount u = current.require(auth);
        repo.findTop30ByUserIdOrderByCreatedAtDesc(u.getId()).forEach(n -> { n.setReadFlag(true); repo.save(n); });
        return "redirect:/notifications";
    }
}