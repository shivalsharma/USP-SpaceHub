package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.UserForm;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.UserRepository;
import fj.ac.usp.spacehub.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    @GetMapping
    String list(Model m) {
        m.addAttribute("users", repo.findAll());
        m.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    @GetMapping("/new")
    String create(Model m) { m.addAttribute("form", new UserForm()); options(m); return "admin/user-form"; }

    @GetMapping("/{id}/edit")
    String edit(@PathVariable Long id, Model m) {
        UserAccount u = repo.findById(id).orElseThrow();
        UserForm f = new UserForm();
        f.setId(u.getId());
        f.setUserCode(u.getUserCode());
        f.setName(u.getName());
        f.setEmail(u.getEmail());
        f.setRole(u.getRole());
        f.setActive(u.isActive());
        f.setDepartment(u.getDepartment());
        f.setMaxDailyBookings(u.getMaxDailyBookings());
        f.setMaxWeeklyBookings(u.getMaxWeeklyBookings());
        m.addAttribute("form", f);
        options(m);
        return "admin/user-form";
    }

    @PostMapping("/save")
    String save(@Valid @ModelAttribute("form") UserForm f, BindingResult br, Model m, RedirectAttributes ra) {
        if (f.getId() == null && (f.getPassword() == null || f.getPassword().length() < 8))
            br.rejectValue("password", "password", "New users require a password of at least 8 characters");
        if (f.getPassword() != null && !f.getPassword().isBlank() && f.getPassword().length() < 8)
            br.rejectValue("password", "password", "Password must be at least 8 characters");
        repo.findByEmailIgnoreCase(f.getEmail()).filter(x -> !Objects.equals(x.getId(), f.getId()))
                .ifPresent(x -> br.rejectValue("email", "duplicate", "Email is already in use"));
        repo.findByUserCodeIgnoreCase(f.getUserCode()).filter(x -> !Objects.equals(x.getId(), f.getId()))
                .ifPresent(x -> br.rejectValue("userCode", "duplicate", "User code is already in use"));
        if (br.hasErrors()) { options(m); return "admin/user-form"; }
        UserAccount u = f.getId() == null ? new UserAccount() : repo.findById(f.getId()).orElseThrow();
        u.setUserCode(f.getUserCode().trim());
        u.setName(f.getName().trim());
        u.setEmail(f.getEmail().trim().toLowerCase());
        u.setRole(f.getRole());
        u.setActive(f.isActive());
        u.setDepartment(f.getDepartment());
        u.setMaxDailyBookings(f.getMaxDailyBookings());
        u.setMaxWeeklyBookings(f.getMaxWeeklyBookings());
        if (f.getPassword() != null && !f.getPassword().isBlank())
            u.setPasswordHash(encoder.encode(f.getPassword()));
        repo.save(u);
        audit.log(f.getId() == null ? "USER_CREATED" : "USER_UPDATED", "User", u.getId(), u.getEmail() + " role=" + u.getRole() + " active=" + u.isActive());
        ra.addFlashAttribute("success", "User saved.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    String toggle(@PathVariable Long id, RedirectAttributes ra) {
        UserAccount u = repo.findById(id).orElseThrow();
        u.setActive(!u.isActive());
        repo.save(u);
        audit.log(u.isActive() ? "USER_REACTIVATED" : "USER_DISABLED", "User", id, u.getEmail());
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    private void options(Model m) {
        m.addAttribute("roles", UserRole.values());
        Set<String> departments = new TreeSet<>(List.of("SITEMP", "ITS", "SAS", "ESTATES", "ENGINEERING", "SCIENCE", "BUSINESS", "LAW", "PACIFIC_TAFE"));
        repo.findAll().stream().map(UserAccount::getDepartment).filter(x -> x != null && !x.isBlank()).forEach(departments::add);
        m.addAttribute("departments", departments);
    }
}