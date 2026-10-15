package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.dto.ScheduleForm;
import fj.ac.usp.spacehub.model.Course;
import fj.ac.usp.spacehub.model.ScheduleEntry;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.model.UserRole;
import fj.ac.usp.spacehub.repository.CourseRepository;
import fj.ac.usp.spacehub.repository.ScheduleEntryRepository;
import fj.ac.usp.spacehub.service.AuditService;
import fj.ac.usp.spacehub.service.CurrentUserService;

import jakarta.transaction.Transactional;
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

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {


    /* =========================================================
       DEPENDENCIES
       ========================================================= */

    private final CurrentUserService current;

    private final ScheduleEntryRepository schedules;

    private final CourseRepository courses;

    private final AuditService audit;

    private final PasswordEncoder passwordEncoder;



    /* =========================================================
       PROFILE PICTURE SETTINGS
       ========================================================= */

    private static final long MAX_PROFILE_PICTURE_SIZE =
            5 * 1024 * 1024;


    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
            );


    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );


    private static final Path PROFILE_PICTURE_DIRECTORY =
            Path.of(
                    "uploads",
                    "profile-pictures"
            )
            .toAbsolutePath()
            .normalize();



    /* =========================================================
       PROFILE PAGE
       GET /profile
       ========================================================= */

    @GetMapping
    String page(
            Authentication auth,
            Model model
    ) {

        UserAccount user =
                current.require(auth);


        /* Current user */

        model.addAttribute(
                "user",
                user
        );


        /* User timetable / busy entries */

        model.addAttribute(
                "entries",
                schedules.findByUserIdOrderByDateAscStartTimeAsc(
                        user.getId()
                )
        );


        /* Empty timetable form */

        model.addAttribute(
                "scheduleForm",
                new ScheduleForm()
        );


        /* -----------------------------------------------------
           Lecturer assigned courses
           ----------------------------------------------------- */

        List<Course> assignedCourses;


        if (
                user.getRole()
                ==
                UserRole.LECTURER
        ) {

            assignedCourses =
                    courses.findActiveCoursesForLecturer(
                            user.getId()
                    );

        } else {

            assignedCourses =
                    List.of();

        }


        model.addAttribute(
                "courses",
                assignedCourses
        );


        return "profile";
    }



    /* =========================================================
       EDIT PROFILE PAGE
       GET /profile/edit
       ========================================================= */

    @GetMapping("/edit")
    String editProfile(
            Authentication auth,
            Model model
    ) {

        UserAccount user =
                current.require(auth);


        model.addAttribute(
                "user",
                user
        );


        return "edit-profile";
    }



    /* =========================================================
       UPDATE PROFILE
       POST /profile/update
       ========================================================= */

    @PostMapping("/update")
    @Transactional
    String updateProfile(
            Authentication auth,

            @RequestParam
            String name,

            @RequestParam(required = false)
            String department,

            @RequestParam(
                    required = false,
                    name = "profilePicture"
            )
            MultipartFile profilePicture,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user =
                current.require(auth);



        /* -----------------------------------------------------
           Validate name
           ----------------------------------------------------- */

        if (
                name == null
                ||
                name.trim().isEmpty()
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Full name cannot be empty."
            );


            return "redirect:/profile/edit";
        }



        /* -----------------------------------------------------
           PROFILE PICTURE
           ----------------------------------------------------- */

        if (
                profilePicture != null
                &&
                !profilePicture.isEmpty()
        ) {

            try {

                String newPicture =
                        saveProfilePicture(
                                profilePicture
                        );


                String oldPicture =
                        user.getProfilePicture();


                user.setProfilePicture(
                        newPicture
                );


                /*
                 * Delete previous image after the
                 * replacement has been successfully stored.
                 */

                deleteOldProfilePicture(
                        oldPicture
                );


                audit.log(
                        "PROFILE_PICTURE_UPDATED",
                        "UserAccount",
                        user.getId(),
                        "Profile picture updated"
                );

            } catch (IllegalArgumentException e) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        e.getMessage()
                );


                return "redirect:/profile/edit";

            } catch (IOException e) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "The profile picture could not be saved."
                );


                return "redirect:/profile/edit";
            }

        }



        /* -----------------------------------------------------
           UPDATE NAME
           ----------------------------------------------------- */

        user.setName(
                name.trim()
        );



        /* -----------------------------------------------------
           UPDATE DEPARTMENT
           ----------------------------------------------------- */

        if (department != null) {

            user.setDepartment(
                    department.trim()
            );

        }



        /*
         * UserAccount is updated while this transaction
         * is active.
         *
         * Hibernate/JPA persists the changes when the
         * transaction completes.
         */



        /* -----------------------------------------------------
           AUDIT LOG
           ----------------------------------------------------- */

        audit.log(
                "PROFILE_UPDATED",
                "UserAccount",
                user.getId(),
                "Profile details updated"
        );



        /* -----------------------------------------------------
           SUCCESS
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully."
        );


        return "redirect:/profile";
    }



    /* =========================================================
       SAVE PROFILE PICTURE
       ========================================================= */

    private String saveProfilePicture(
            MultipartFile file
    ) throws IOException {


        /* -----------------------------------------------------
           FILE SIZE
           ----------------------------------------------------- */

        if (
                file.getSize()
                >
                MAX_PROFILE_PICTURE_SIZE
        ) {

            throw new IllegalArgumentException(
                    "Profile picture must be 5 MB or smaller."
            );

        }



        /* -----------------------------------------------------
           ORIGINAL FILE NAME
           ----------------------------------------------------- */

        String originalFilename =
                file.getOriginalFilename();


        if (
                originalFilename == null
                ||
                originalFilename.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Please select a valid image file."
            );

        }



        /* -----------------------------------------------------
           FILE EXTENSION
           ----------------------------------------------------- */

        String extension =
                getFileExtension(
                        originalFilename
                );


        if (
                !ALLOWED_IMAGE_EXTENSIONS.contains(
                        extension
                )
        ) {

            throw new IllegalArgumentException(
                    "Only JPG, JPEG, PNG and WEBP images are allowed."
            );

        }



        /* -----------------------------------------------------
           MIME TYPE
           ----------------------------------------------------- */

        String contentType =
                file.getContentType();


        if (
                contentType == null
                ||
                !ALLOWED_IMAGE_TYPES.contains(
                        contentType.toLowerCase(
                                Locale.ROOT
                        )
                )
        ) {

            throw new IllegalArgumentException(
                    "The selected file is not a supported image."
            );

        }



        /* -----------------------------------------------------
           CREATE UPLOAD DIRECTORY
           ----------------------------------------------------- */

        Files.createDirectories(
                PROFILE_PICTURE_DIRECTORY
        );



        /* -----------------------------------------------------
           UNIQUE FILE NAME
           ----------------------------------------------------- */

        String storedFilename =
                UUID.randomUUID()
                        +
                "."
                        +
                extension;



        /* -----------------------------------------------------
           TARGET FILE
           ----------------------------------------------------- */

        Path targetFile =
                PROFILE_PICTURE_DIRECTORY
                        .resolve(
                                storedFilename
                        )
                        .normalize();



        /* -----------------------------------------------------
           PATH SECURITY CHECK
           ----------------------------------------------------- */

        if (
                !targetFile.startsWith(
                        PROFILE_PICTURE_DIRECTORY
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid profile picture path."
            );

        }



        /* -----------------------------------------------------
           SAVE FILE
           ----------------------------------------------------- */

        try (
                var inputStream =
                        file.getInputStream()
        ) {

            Files.copy(
                    inputStream,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

        }


        return storedFilename;
    }



    /* =========================================================
       DELETE OLD PROFILE PICTURE
       ========================================================= */

    private void deleteOldProfilePicture(
            String oldPicture
    ) {

        if (
                oldPicture == null
                ||
                oldPicture.isBlank()
        ) {

            return;
        }


        try {

            Path oldFile =
                    PROFILE_PICTURE_DIRECTORY
                            .resolve(
                                    oldPicture
                            )
                            .normalize();


            if (
                    oldFile.startsWith(
                            PROFILE_PICTURE_DIRECTORY
                    )
            ) {

                Files.deleteIfExists(
                        oldFile
                );

            }

        } catch (IOException ignored) {

            /*
             * An old image failing to delete should
             * not prevent the profile update.
             */

        }

    }



    /* =========================================================
       GET FILE EXTENSION
       ========================================================= */

    private String getFileExtension(
            String filename
    ) {

        int dot =
                filename.lastIndexOf('.');


        if (
                dot < 0
                ||
                dot == filename.length() - 1
        ) {

            throw new IllegalArgumentException(
                    "Please select a valid image file."
            );

        }


        return filename
                .substring(
                        dot + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }



    /* =========================================================
       CHANGE PASSWORD PAGE
       GET /profile/change-password
       ========================================================= */

    @GetMapping("/change-password")
    String changePasswordPage(
            Authentication auth,
            Model model
    ) {

        UserAccount user =
                current.require(auth);


        model.addAttribute(
                "user",
                user
        );


        return "change-password";
    }



    /* =========================================================
       CHANGE PASSWORD
       POST /profile/change-password
       ========================================================= */

    @PostMapping("/change-password")
    @Transactional
    String changePassword(
            Authentication auth,

            @RequestParam
            String currentPassword,

            @RequestParam
            String newPassword,

            @RequestParam
            String confirmPassword,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user =
                current.require(auth);



        /* -----------------------------------------------------
           VALIDATE REQUIRED FIELDS
           ----------------------------------------------------- */

        if (
                currentPassword == null
                ||
                currentPassword.isBlank()
                ||
                newPassword == null
                ||
                newPassword.isBlank()
                ||
                confirmPassword == null
                ||
                confirmPassword.isBlank()
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please complete all password fields."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           VERIFY CURRENT PASSWORD
           ----------------------------------------------------- */

        if (
                !passwordEncoder.matches(
                        currentPassword,
                        user.getPasswordHash()
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Your current password is incorrect."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           CONFIRM NEW PASSWORD
           ----------------------------------------------------- */

        if (
                !newPassword.equals(
                        confirmPassword
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "New password and confirmation do not match."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           NEW PASSWORD MUST BE DIFFERENT
           ----------------------------------------------------- */

        if (
                passwordEncoder.matches(
                        newPassword,
                        user.getPasswordHash()
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Your new password must be different from your current password."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           MINIMUM LENGTH
           ----------------------------------------------------- */

        if (
                newPassword.length()
                <
                8
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Password must contain at least 8 characters."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           UPPERCASE LETTER
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*[A-Z].*"
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Password must contain at least one uppercase letter."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           LOWERCASE LETTER
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*[a-z].*"
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Password must contain at least one lowercase letter."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           NUMBER
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*\\d.*"
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Password must contain at least one number."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           SPECIAL CHARACTER
           ----------------------------------------------------- */

        if (
                !newPassword.matches(
                        ".*[^A-Za-z0-9].*"
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Password must contain at least one special character."
            );


            return "redirect:/profile/change-password";
        }



        /* -----------------------------------------------------
           ENCODE NEW PASSWORD
           ----------------------------------------------------- */

        String encodedPassword =
                passwordEncoder.encode(
                        newPassword
                );



        /* -----------------------------------------------------
           SAVE HASH
           ----------------------------------------------------- */

        user.setPasswordHash(
                encodedPassword
        );


        /*
         * Only the encoded BCrypt password is stored.
         *
         * The plain current/new passwords are never
         * written to the database.
         */



        /* -----------------------------------------------------
           AUDIT LOG
           ----------------------------------------------------- */

        audit.log(
                "PASSWORD_CHANGED",
                "UserAccount",
                user.getId(),
                "User changed their account password"
        );



        /* -----------------------------------------------------
           SUCCESS MESSAGE
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Password changed successfully."
        );


        return "redirect:/profile";
    }



    /* =========================================================
       ADD TIMETABLE / BUSY TIME
       POST /profile/schedule
       ========================================================= */

    @PostMapping("/schedule")
    String add(
            Authentication auth,

            @Valid
            @ModelAttribute
            ScheduleForm scheduleForm,

            BindingResult bindingResult,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user =
                current.require(auth);



        /* -----------------------------------------------------
           VALIDATE SCHEDULE
           ----------------------------------------------------- */

        if (
                bindingResult.hasErrors()
                ||
                scheduleForm.getStartTime() == null
                ||
                scheduleForm.getEndTime() == null
                ||
                !scheduleForm
                        .getEndTime()
                        .isAfter(
                                scheduleForm.getStartTime()
                        )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Enter a valid schedule date/time."
            );


            return "redirect:/profile";
        }



        /* -----------------------------------------------------
           CREATE SCHEDULE ENTRY
           ----------------------------------------------------- */

        ScheduleEntry scheduleEntry =
                new ScheduleEntry();


        scheduleEntry.setUser(
                user
        );


        scheduleEntry.setDate(
                scheduleForm.getDate()
        );


        scheduleEntry.setStartTime(
                scheduleForm.getStartTime()
        );


        scheduleEntry.setEndTime(
                scheduleForm.getEndTime()
        );


        scheduleEntry.setTitle(
                scheduleForm.getTitle()
        );



        /* -----------------------------------------------------
           SAVE
           ----------------------------------------------------- */

        schedules.save(
                scheduleEntry
        );



        /* -----------------------------------------------------
           AUDIT
           ----------------------------------------------------- */

        audit.log(
                "TIMETABLE_ENTRY_CREATED",
                "ScheduleEntry",
                scheduleEntry.getId(),
                scheduleEntry.getTitle()
        );



        /* -----------------------------------------------------
           SUCCESS
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Busy/timetable entry added."
        );


        return "redirect:/profile";
    }



    /* =========================================================
       DELETE TIMETABLE / BUSY TIME
       POST /profile/schedule/{id}/delete
       ========================================================= */

    @PostMapping("/schedule/{id}/delete")
    String deleteSchedule(
            Authentication auth,

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {

        UserAccount user =
                current.require(auth);



        /* -----------------------------------------------------
           FIND ENTRY
           ----------------------------------------------------- */

        ScheduleEntry scheduleEntry =
                schedules
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Schedule entry not found."
                                        )
                        );



        /* -----------------------------------------------------
           OWNERSHIP SECURITY CHECK
           ----------------------------------------------------- */

        if (
                !scheduleEntry
                        .getUser()
                        .getId()
                        .equals(
                                user.getId()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Not your schedule entry."
            );

        }



        String scheduleTitle =
                scheduleEntry.getTitle();



        /* -----------------------------------------------------
           DELETE
           ----------------------------------------------------- */

        schedules.delete(
                scheduleEntry
        );



        /* -----------------------------------------------------
           AUDIT
           ----------------------------------------------------- */

        audit.log(
                "TIMETABLE_ENTRY_DELETED",
                "ScheduleEntry",
                id,
                scheduleTitle
        );



        /* -----------------------------------------------------
           SUCCESS
           ----------------------------------------------------- */

        redirectAttributes.addFlashAttribute(
                "success",
                "Schedule entry removed."
        );


        return "redirect:/profile";
    }

}