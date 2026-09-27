package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import fj.ac.usp.spacehub.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
public class AdminRoomController {
    private final RoomRepository rooms;
    private final MaintenanceRepository maintenance;
    private final UserRepository users;
    private final CourseRepository courses;
    private final AuditService audit;

    @GetMapping
    String list(Model m) { m.addAttribute("rooms", rooms.findAllByOrderByCode()); return "admin/rooms"; }

    @GetMapping("/new")
    String create(Model m) { m.addAttribute("form", new RoomForm()); m.addAttribute("maintenanceForm", new MaintenanceForm()); options(m); return "admin/room-form"; }

    @GetMapping("/{id}/edit")
    String edit(@PathVariable Long id, Model m) {
        Room r = rooms.findById(id).orElseThrow();
        RoomForm f = new RoomForm();
        f.setId(r.getId());
        f.setCode(r.getCode());
        f.setName(r.getName());
        f.setBuilding(r.getBuilding());
        f.setCapacity(r.getCapacity());
        f.setComputerCount(r.getComputerCount());
        f.setOperationalComputers(r.getOperationalComputers());
        f.setProjectorAvailable(r.isProjectorAvailable());
        f.setProjectorOperational(r.isProjectorOperational());
        f.setWhiteboardAvailable(r.isWhiteboardAvailable());
        f.setWhiteboardOperational(r.isWhiteboardOperational());
        f.setAccessLevel(r.getAccessLevel());
        f.setStatus(r.getStatus());
        f.setDepartmentRestriction(r.getDepartmentRestriction());
        f.setCourseRestriction(r.getCourseRestriction());
        f.setFacilitiesCsv(FacilityUtil.toCsv(r.getFacilities()));
        f.setFaultyFacilitiesCsv(FacilityUtil.toCsv(r.getFaultyFacilities()));
        m.addAttribute("form", f);
        m.addAttribute("maintenance", maintenance.findByRoomIdOrderByDateDescStartTimeAsc(id));
        m.addAttribute("maintenanceForm", new MaintenanceForm());
        options(m);
        return "admin/room-form";
    }

    @PostMapping("/save")
    String save(@Valid @ModelAttribute("form") RoomForm f, BindingResult br, Model m, RedirectAttributes ra) {
        if (f.getOperationalComputers() > f.getComputerCount())
            br.rejectValue("operationalComputers", "range", "Operational computers cannot exceed total computers");
        rooms.findByCodeIgnoreCase(f.getCode()).filter(x -> !Objects.equals(x.getId(), f.getId()))
                .ifPresent(x -> br.rejectValue("code", "duplicate", "Room code is already in use"));
        if (br.hasErrors()) { m.addAttribute("maintenanceForm", new MaintenanceForm()); options(m); return "admin/room-form"; }
        Room r = f.getId() == null ? new Room() : rooms.findById(f.getId()).orElseThrow();
        r.setCode(f.getCode().trim().toUpperCase());
        r.setName(f.getName().trim());
        r.setBuilding(f.getBuilding().trim());
        r.setCapacity(f.getCapacity());
        r.setComputerCount(f.getComputerCount());
        r.setOperationalComputers(f.getOperationalComputers());
        r.setProjectorAvailable(f.isProjectorAvailable());
        r.setProjectorOperational(f.isProjectorAvailable() && f.isProjectorOperational());
        r.setWhiteboardAvailable(f.isWhiteboardAvailable());
        r.setWhiteboardOperational(f.isWhiteboardAvailable() && f.isWhiteboardOperational());
        r.setAccessLevel(f.getAccessLevel());
        r.setStatus(f.getStatus());
        r.setDepartmentRestriction(blankToNull(f.getDepartmentRestriction()));
        r.setCourseRestriction(blankToNull(f.getCourseRestriction()));
        r.setFacilities(FacilityUtil.parseCsv(f.getFacilitiesCsv()));
        r.setFaultyFacilities(FacilityUtil.parseCsv(f.getFaultyFacilitiesCsv()));
        rooms.save(r);
        audit.log(f.getId() == null ? "ROOM_CREATED" : "ROOM_UPDATED", "Room", r.getId(), r.getCode() + " status=" + r.getStatus());
        ra.addFlashAttribute("success", "Room saved.");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/{id}/maintenance")
    String addMaintenance(@PathVariable Long id, @Valid @ModelAttribute MaintenanceForm f, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors() || !f.getEndTime().isAfter(f.getStartTime())) {
            ra.addFlashAttribute("error", "Enter a valid maintenance window.");
            return "redirect:/admin/rooms/" + id + "/edit";
        }
        MaintenanceWindow w = new MaintenanceWindow();
        w.setRoom(rooms.findById(id).orElseThrow());
        w.setDate(f.getDate());
        w.setStartTime(f.getStartTime());
        w.setEndTime(f.getEndTime());
        w.setReason(f.getReason());
        maintenance.save(w);
        audit.log("ROOM_MAINTENANCE_ADDED", "Room", id, f.getReason());
        ra.addFlashAttribute("success", "Maintenance window added.");
        return "redirect:/admin/rooms/" + id + "/edit";
    }

    @PostMapping("/{roomId}/maintenance/{id}/delete")
    String delMaintenance(@PathVariable Long roomId, @PathVariable Long id, RedirectAttributes ra) {
        maintenance.deleteById(id);
        audit.log("ROOM_MAINTENANCE_REMOVED", "Room", roomId, "Maintenance window #" + id);
        ra.addFlashAttribute("success", "Maintenance window removed.");
        return "redirect:/admin/rooms/" + roomId + "/edit";
    }

    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private void options(Model m) {
        m.addAttribute("accessLevels", AccessLevel.values());
        m.addAttribute("roomStatuses", RoomStatus.values());
        m.addAttribute("facilityOptions", FacilityUtil.additionalOptions());
        m.addAttribute("maintenanceReasons", List.of("Scheduled maintenance", "Equipment repair", "Electrical work", "Network maintenance", "Cleaning / inspection", "Room unavailable"));
        Set<String> buildings = new TreeSet<>(List.of("ICT Building", "Engineering Building", "Student Centre", "Library Building", "Science Building", "Business Building"));
        rooms.findAll().stream().map(Room::getBuilding).filter(Objects::nonNull).forEach(buildings::add);
        m.addAttribute("buildings", buildings);
        Set<String> departments = new TreeSet<>();
        users.findAll().stream().map(UserAccount::getDepartment).filter(x -> x != null && !x.isBlank()).forEach(departments::add);
        m.addAttribute("departments", departments);
        m.addAttribute("courseOptions", courses.findByActiveTrueOrderByCode());
    }
}