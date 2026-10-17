package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.UserForm;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.model.UserRole;
import fj.ac.usp.spacehub.repository.UserRepository;
import fj.ac.usp.spacehub.service.AuditService;
import fj.ac.usp.spacehub.service.EmailService;
import fj.ac.usp.spacehub.service.TemporaryPasswordService;

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


    /* =========================================================
       DEPENDENCIES
       ========================================================= */

    private final UserRepository repo;

    private final PasswordEncoder encoder;

    private final AuditService audit;

    private final EmailService emailService;

    private final TemporaryPasswordService temporaryPasswordService;



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

            Model model
    ) {


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
    public String create(
            Model model
    ) {


        UserForm form =
                new UserForm();


        /*
         * New users are active by default.
         */
        form.setActive(
                true
        );


        model.addAttribute(
                "form",
                form
        );


        options(
                model
        );


        return "admin/user-form";
    }



    /* =========================================================
       EDIT USER
       ========================================================= */

    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable
            Long id,

            Model model
    ) {


        UserAccount user =
                repo
                        .findById(
                                id
                        )
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


        options(
                model
        );


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

            RedirectAttributes redirectAttributes
    ) {


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

            options(
                    model
            );


            return "admin/user-form";
        }



        /* =====================================================
           CREATE OR LOAD USER
           ===================================================== */

        boolean newUser =
                form.getId() == null;


        UserAccount user;


        /*
         * Only used while creating a new account.
         *
         * The plain temporary password is never written
         * to the database.
         */
        String temporaryPassword =
                null;



        if (newUser) {


            user =
                    new UserAccount();



            /* -------------------------------------------------
               GENERATE TEMPORARY PASSWORD
               ------------------------------------------------- */

            temporaryPassword =
                    temporaryPasswordService.generate();



            /* -------------------------------------------------
               STORE ONLY BCRYPT HASH
               ------------------------------------------------- */

            user.setPasswordHash(

                    encoder.encode(
                            temporaryPassword
                    )

            );



            /* -------------------------------------------------
               FORCE PASSWORD CHANGE
               ------------------------------------------------- */

            /*
             * A newly-created account is using a temporary
             * password.
             *
             * The user must replace it with their own password
             * after signing in.
             */
            user.setMustChangePassword(
                    true
            );


        } else {


            user =
                    repo
                            .findById(
                                    form.getId()
                            )
                            .orElseThrow();

        }



        /* =====================================================
           UPDATE USER DETAILS
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



        /* =====================================================
           DEPARTMENT
           ===================================================== */

        user.setDepartment(

                form.getDepartment() == null

                        ? null

                        : form
                                .getDepartment()
                                .trim()

        );



        /* =====================================================
           BOOKING LIMITS
           ===================================================== */

        user.setMaxDailyBookings(
                form.getMaxDailyBookings()
        );


        user.setMaxWeeklyBookings(
                form.getMaxWeeklyBookings()
        );



        /* =====================================================
           SAVE USER
           ===================================================== */

        repo.save(
                user
        );



        /* =====================================================
           NEW USER WELCOME EMAIL
           ===================================================== */

        if (newUser) {


            try {


                /*
                 * Sends the temporary password to the user's
                 * registered email address.
                 */
                emailService.sendNewAccountEmail(
                        user,
                        temporaryPassword
                );


            } catch (Exception exception) {


                /*
                 * If the welcome email cannot be sent, the user
                 * would have no way of knowing the automatically
                 * generated temporary password.
                 *
                 * Remove the newly-created account so that the
                 * administrator can correct the email/SMTP
                 * configuration and safely try again.
                 */
                repo.delete(
                        user
                );


                redirectAttributes.addFlashAttribute(

                        "error",

                        "The user was not created because the welcome email "
                                +
                        "could not be sent. Check the email configuration "
                                +
                        "and try again."

                );


                return "redirect:/admin/users/new";

            }

        }



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

                        + " mustChangePassword="

                        + user.isMustChangePassword()

        );



        /* =====================================================
           SUCCESS MESSAGE
           ===================================================== */

        redirectAttributes.addFlashAttribute(

                "success",

                newUser

                        ? "User created successfully and the temporary password was emailed."

                        : "User updated successfully."

        );


        return "redirect:/admin/users";
    }



    /* =========================================================
       DISABLE / REACTIVATE USER
       ========================================================= */

    @PostMapping("/{id}/toggle")
    public String toggle(
            @PathVariable
            Long id,

            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {


        UserAccount user =
                repo
                        .findById(
                                id
                        )
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


        repo.save(
                user
        );



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
            String search
    ) {


        return

                field != null

                &&

                field
                        .toLowerCase()
                        .contains(
                                search
                        );
    }



    /* =========================================================
       FORM OPTIONS
       ========================================================= */

    private void options(
            Model model
    ) {


        /* -----------------------------------------------------
           USER ROLES
           ----------------------------------------------------- */

        model.addAttribute(

                "roles",

                UserRole.values()

        );



        /* -----------------------------------------------------
           DEPARTMENTS
           ----------------------------------------------------- */

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



        /*
         * Add any departments already stored in the database
         * so an existing/custom department is not lost.
         */
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