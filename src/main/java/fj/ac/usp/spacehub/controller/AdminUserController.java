package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.UserForm;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.model.UserRole;
import fj.ac.usp.spacehub.repository.UserRepository;
import fj.ac.usp.spacehub.service.AuditService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository repo;

    private final PasswordEncoder encoder;

    private final AuditService audit;



    /* =========================================================
       USER MANAGEMENT LIST
       ========================================================= */
    @GetMapping
    public String list(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            UserRole role,

            @RequestParam(required = false)
            String status,

            Authentication authentication,

            Model model) {


        List<UserAccount> allUsers =
                repo.findAll();



        /* =====================================================
           SUMMARY COUNTS
           ===================================================== */

        long totalUsers =
                allUsers.size();


        long students =
                allUsers
                        .stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == UserRole.STUDENT
                        )
                        .count();


        long lecturers =
                allUsers
                        .stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == UserRole.LECTURER
                        )
                        .count();


        long administrators =
                allUsers
                        .stream()
                        .filter(
                                user ->

                                        user.getRole()
                                                == UserRole.ADMIN

                                        ||

                                        user.getRole()
                                                == UserRole.IT_ADMIN
                        )
                        .count();



        /* =====================================================
           SEARCH AND FILTER
           ===================================================== */

        List<UserAccount> filteredUsers =
                allUsers
                        .stream()


                        .filter(user -> {

                            if (
                                    search == null
                                    ||
                                    search.isBlank()
                            ) {

                                return true;

                            }


                            String value =
                                    search
                                            .trim()
                                            .toLowerCase();


                            return

                                    containsIgnoreCase(
                                            user.getUserCode(),
                                            value
                                    )

                                    ||

                                    containsIgnoreCase(
                                            user.getName(),
                                            value
                                    )

                                    ||

                                    containsIgnoreCase(
                                            user.getEmail(),
                                            value
                                    );

                        })


                        .filter(
                                user ->

                                        role == null

                                        ||

                                        user.getRole()
                                                == role
                        )


                        .filter(user -> {

                            if (
                                    status == null
                                    ||
                                    status.isBlank()
                            ) {

                                return true;

                            }


                            if (
                                    "ACTIVE"
                                            .equalsIgnoreCase(
                                                    status
                                            )
                            ) {

                                return user.isActive();

                            }


                            if (
                                    "DISABLED"
                                            .equalsIgnoreCase(
                                                    status
                                            )
                            ) {

                                return !user.isActive();

                            }


                            return true;

                        })


                        .sorted(

                                Comparator.comparing(

                                        UserAccount::getName,

                                        String.CASE_INSENSITIVE_ORDER

                                )

                        )


                        .collect(
                                Collectors.toList()
                        );



        /* =====================================================
           CURRENT LOGGED-IN USER
           ===================================================== */

        String currentUserEmail =

                authentication != null

                        ? authentication.getName()

                        : "";



        /* =====================================================
           SEND DATA TO PAGE
           ===================================================== */

        model.addAttribute(
                "users",
                filteredUsers
        );


        model.addAttribute(
                "roles",
                UserRole.values()
        );


        model.addAttribute(
                "search",
                search
        );


        model.addAttribute(
                "selectedRole",
                role
        );


        model.addAttribute(
                "selectedStatus",
                status
        );


        model.addAttribute(
                "totalUsers",
                totalUsers
        );


        model.addAttribute(
                "studentCount",
                students
        );


        model.addAttribute(
                "lecturerCount",
                lecturers
        );


        model.addAttribute(
                "adminCount",
                administrators
        );


        model.addAttribute(
                "currentUserEmail",
                currentUserEmail
        );


        return "admin/users";
    }



    /* =========================================================
       ADD USER
       ========================================================= */
    @GetMapping("/new")
    public String create(Model model) {


        UserForm form =
                new UserForm();


        /*
         * New users are active by default.
         */
        form.setActive(true);


        model.addAttribute(
                "form",
                form
        );


        options(model);


        return "admin/user-form";
    }



    /* =========================================================
       EDIT USER
       ========================================================= */
    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            Model model) {


        UserAccount user =
                repo
                        .findById(id)
                        .orElseThrow();


        UserForm form =
                new UserForm();


        form.setId(
                user.getId()
        );


        form.setUserCode(
                user.getUserCode()
        );


        form.setName(
                user.getName()
        );


        form.setEmail(
                user.getEmail()
        );


        form.setRole(
                user.getRole()
        );


        form.setActive(
                user.isActive()
        );


        form.setDepartment(
                user.getDepartment()
        );


        form.setMaxDailyBookings(
                user.getMaxDailyBookings()
        );


        form.setMaxWeeklyBookings(
                user.getMaxWeeklyBookings()
        );


        model.addAttribute(
                "form",
                form
        );


        options(model);


        return "admin/user-form";
    }



    /* =========================================================
       SAVE USER
       ========================================================= */
    @PostMapping("/save")
    public String save(
            @Valid
            @ModelAttribute("form")
            UserForm form,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes) {


        /* =====================================================
           NEW USER PASSWORD
           ===================================================== */

        if (

                form.getId() == null

                &&

                (
                        form.getPassword() == null

                        ||

                        form.getPassword()
                                .length() < 8
                )

        ) {

            bindingResult.rejectValue(

                    "password",

                    "password",

                    "New users require a password of at least 8 characters"

            );

        }



        /* =====================================================
           PASSWORD LENGTH
           ===================================================== */

        if (

                form.getPassword() != null

                &&

                !form.getPassword()
                        .isBlank()

                &&

                form.getPassword()
                        .length() < 8

        ) {

            bindingResult.rejectValue(

                    "password",

                    "password",

                    "Password must be at least 8 characters"

            );

        }



        /* =====================================================
           DUPLICATE EMAIL
           ===================================================== */

        repo
                .findByEmailIgnoreCase(
                        form.getEmail()
                )

                .filter(
                        existingUser ->

                                !Objects.equals(

                                        existingUser.getId(),

                                        form.getId()

                                )
                )

                .ifPresent(
                        existingUser ->

                                bindingResult.rejectValue(

                                        "email",

                                        "duplicate",

                                        "Email is already in use"

                                )
                );



        /* =====================================================
           DUPLICATE USER / STAFF ID
           ===================================================== */

        repo
                .findByUserCodeIgnoreCase(
                        form.getUserCode()
                )

                .filter(
                        existingUser ->

                                !Objects.equals(

                                        existingUser.getId(),

                                        form.getId()

                                )
                )

                .ifPresent(
                        existingUser ->

                                bindingResult.rejectValue(

                                        "userCode",

                                        "duplicate",

                                        "User code is already in use"

                                )
                );



        /* =====================================================
           VALIDATION ERRORS
           ===================================================== */

        if (
                bindingResult.hasErrors()
        ) {

            options(model);

            return "admin/user-form";
        }



        /* =====================================================
           CREATE OR LOAD USER
           ===================================================== */

        boolean newUser =
                form.getId() == null;


        UserAccount user;


        if (newUser) {

            user =
                    new UserAccount();

        } else {

            user =
                    repo
                            .findById(
                                    form.getId()
                            )
                            .orElseThrow();

        }



        /* =====================================================
           UPDATE DETAILS
           ===================================================== */

        user.setUserCode(

                form
                        .getUserCode()
                        .trim()

        );


        user.setName(

                form
                        .getName()
                        .trim()

        );


        user.setEmail(

                form
                        .getEmail()
                        .trim()
                        .toLowerCase()

        );


        user.setRole(
                form.getRole()
        );


        user.setActive(
                form.isActive()
        );


        user.setDepartment(
                form.getDepartment()
        );


        user.setMaxDailyBookings(
                form.getMaxDailyBookings()
        );


        user.setMaxWeeklyBookings(
                form.getMaxWeeklyBookings()
        );



        /* =====================================================
           PASSWORD
           ===================================================== */

        if (

                form.getPassword() != null

                &&

                !form.getPassword()
                        .isBlank()

        ) {

            user.setPasswordHash(

                    encoder.encode(
                            form.getPassword()
                    )

            );

        }



        /* =====================================================
           SAVE USER
           ===================================================== */

        repo.save(user);



        /* =====================================================
           AUDIT LOG
           ===================================================== */

        audit.log(

                newUser

                        ? "USER_CREATED"

                        : "USER_UPDATED",


                "User",


                user.getId(),


                user.getEmail()

                        + " role="

                        + user.getRole()

                        + " active="

                        + user.isActive()

        );



        /* =====================================================
           SUCCESS MESSAGE
           ===================================================== */

        redirectAttributes.addFlashAttribute(

                "success",

                newUser

                        ? "User created successfully."

                        : "User updated successfully."

        );


        return "redirect:/admin/users";
    }



    /* =========================================================
       DISABLE / REACTIVATE USER
       ========================================================= */
    @PostMapping("/{id}/toggle")
    public String toggle(
            @PathVariable Long id,

            Authentication authentication,

            RedirectAttributes redirectAttributes) {


        UserAccount user =
                repo
                        .findById(id)
                        .orElseThrow();



        /* =====================================================
           PREVENT SELF-DISABLE
           ===================================================== */

        if (

                authentication != null

                &&

                authentication
                        .getName()
                        .equalsIgnoreCase(
                                user.getEmail()
                        )

                &&

                user.isActive()

        ) {

            redirectAttributes.addFlashAttribute(

                    "error",

                    "You cannot disable your own account."

            );


            return "redirect:/admin/users";
        }



        /* =====================================================
           CHANGE STATUS
           ===================================================== */

        user.setActive(
                !user.isActive()
        );


        repo.save(user);



        /* =====================================================
           AUDIT
           ===================================================== */

        audit.log(

                user.isActive()

                        ? "USER_REACTIVATED"

                        : "USER_DISABLED",


                "User",


                id,


                user.getEmail()

        );



        /* =====================================================
           SUCCESS MESSAGE
           ===================================================== */

        redirectAttributes.addFlashAttribute(

                "success",

                user.isActive()

                        ? "User account reactivated."

                        : "User account disabled."

        );


        return "redirect:/admin/users";
    }



    /* =========================================================
       SEARCH HELPER
       ========================================================= */
    private boolean containsIgnoreCase(
            String field,
            String search) {


        return

                field != null

                &&

                field
                        .toLowerCase()
                        .contains(search);
    }



    /* =========================================================
       FORM OPTIONS
       ========================================================= */
    private void options(
            Model model) {


        model.addAttribute(

                "roles",

                UserRole.values()

        );



        Set<String> departments =
                new TreeSet<>(

                        List.of(

                                "SITEMP",

                                "ITS",

                                "SAS",

                                "ESTATES",

                                "ENGINEERING",

                                "SCIENCE",

                                "BUSINESS",

                                "LAW",

                                "PACIFIC_TAFE"

                        )

                );



        repo
                .findAll()

                .stream()

                .map(
                        UserAccount::getDepartment
                )

                .filter(

                        department ->

                                department != null

                                &&

                                !department.isBlank()

                )

                .forEach(
                        departments::add
                );



        model.addAttribute(

                "departments",

                departments

        );
    }

}