package fj.ac.usp.spacehub.controller;

import fj.ac.usp.spacehub.model.Booking;
import fj.ac.usp.spacehub.model.BookingStatus;
import fj.ac.usp.spacehub.model.Room;
import fj.ac.usp.spacehub.model.RoomStatus;
import fj.ac.usp.spacehub.model.UserAccount;
import fj.ac.usp.spacehub.model.UserRole;

import fj.ac.usp.spacehub.repository.BookingRepository;
import fj.ac.usp.spacehub.repository.CourseRepository;
import fj.ac.usp.spacehub.repository.NotificationRepository;
import fj.ac.usp.spacehub.repository.RoomRepository;
import fj.ac.usp.spacehub.repository.UserRepository;

import fj.ac.usp.spacehub.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.ZoneId;

import java.util.Comparator;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class HomeController {


    /* =========================================================
       SERVICES / REPOSITORIES
       ========================================================= */

    private final CurrentUserService current;

    private final BookingRepository bookings;

    private final NotificationRepository notifications;

    private final CourseRepository courses;

    private final UserRepository users;

    private final RoomRepository rooms;



    /* =========================================================
       LOGIN PAGE
       ========================================================= */

    @GetMapping("/login")
    public String login() {

        return "login";
    }



    /* =========================================================
       ROLE-AWARE DASHBOARD
       ========================================================= */

    @GetMapping({"/", "/dashboard"})
    public String dashboard(
            Authentication authentication,
            Model model) {


        /*
         * Get the currently logged-in user.
         */
        UserAccount user =
                current.require(authentication);


        /*
         * Current Fiji date.
         */
        LocalDate today =
                LocalDate.now(
                        ZoneId.of("Pacific/Fiji")
                );


        /*
         * Always make the logged-in user available
         * to dashboard.html.
         */
        model.addAttribute(
                "user",
                user
        );


        /*
         * Notifications belong to the logged-in user
         * regardless of role.
         */
        model.addAttribute(
                "unread",
                notifications.countByUserIdAndReadFlagFalse(
                        user.getId()
                )
        );


        /*
         * Defaults.
         *
         * These ensure Thymeleaf always has values
         * even when a specific role does not use them.
         */
        model.addAttribute(
                "pending",
                0L
        );

        model.addAttribute(
                "approved",
                0L
        );

        model.addAttribute(
                "upcoming",
                List.of()
        );

        model.addAttribute(
                "courses",
                List.of()
        );


        /*
         * =====================================================
         * ADMIN DASHBOARD
         * =====================================================
         *
         * ADMIN sees system-wide booking and system statistics.
         */
        if (user.getRole() == UserRole.ADMIN) {


            List<Booking> allBookings =
                    bookings.findAll();


            /*
             * Booking requests waiting for administrator review.
             */
            long pendingApprovals =
                    allBookings
                            .stream()
                            .filter(
                                    booking ->
                                            booking.getStatus()
                                                    == BookingStatus.PENDING
                            )
                            .count();


            /*
             * All approved bookings.
             */
            long approvedBookings =
                    allBookings
                            .stream()
                            .filter(
                                    booking ->
                                            booking.getStatus()
                                                    == BookingStatus.APPROVED
                            )
                            .count();


            /*
             * Next five approved bookings across the system.
             */
            List<Booking> upcomingBookings =
                    allBookings
                            .stream()
                            .filter(
                                    booking ->

                                            booking.getStatus()
                                                    == BookingStatus.APPROVED

                                            &&

                                            !booking.getDate()
                                                    .isBefore(today)
                            )
                            .sorted(

                                    Comparator
                                            .comparing(
                                                    Booking::getDate
                                            )
                                            .thenComparing(
                                                    Booking::getStartTime
                                            )

                            )
                            .limit(5)
                            .toList();


            model.addAttribute(
                    "pending",
                    pendingApprovals
            );


            model.addAttribute(
                    "approved",
                    approvedBookings
            );


            model.addAttribute(
                    "upcoming",
                    upcomingBookings
            );


            model.addAttribute(
                    "totalUsers",
                    users.count()
            );


            model.addAttribute(
                    "totalRooms",
                    rooms.count()
            );


            model.addAttribute(
                    "totalCourses",
                    courses.count()
            );


            return "dashboard";
        }



        /*
         * =====================================================
         * IT ADMIN DASHBOARD
         * =====================================================
         *
         * IT_ADMIN focuses on:
         *
         * - user accounts
         * - active users
         * - room administration
         * - room issues / maintenance
         */
        if (user.getRole() == UserRole.IT_ADMIN) {


            List<UserAccount> allUsers =
                    users.findAll();


            List<Room> allRooms =
                    rooms.findAll();


            /*
             * Number of active user accounts.
             */
            long activeUsers =
                    allUsers
                            .stream()
                            .filter(
                                    UserAccount::isActive
                            )
                            .count();


            /*
             * Rooms requiring attention.
             *
             * Any room that is not ACTIVE is counted.
             *
             * This includes:
             * - DISABLED
             * - MAINTENANCE
             */
            long roomsNeedingAttention =
                    allRooms
                            .stream()
                            .filter(
                                    room ->

                                            room.getStatus()
                                                    != RoomStatus.ACTIVE
                            )
                            .count();


            model.addAttribute(
                    "totalUsers",
                    (long) allUsers.size()
            );


            model.addAttribute(
                    "activeUsers",
                    activeUsers
            );


            model.addAttribute(
                    "totalRooms",
                    (long) allRooms.size()
            );


            model.addAttribute(
                    "roomsNeedingAttention",
                    roomsNeedingAttention
            );


            return "dashboard";
        }



        /*
         * =====================================================
         * STUDENT / LECTURER DASHBOARD
         * =====================================================
         *
         * These roles see statistics based only on
         * their own booking requests.
         */

        List<Booking> myBookings =
                bookings
                        .findByRequesterIdOrderByDateDescStartTimeDesc(
                                user.getId()
                        );



        /*
         * Pending includes:
         *
         * - normal pending requests
         * - requests where an alternative was proposed
         */
        long pendingBookings =
                myBookings
                        .stream()
                        .filter(
                                booking ->

                                        booking.getStatus()
                                                == BookingStatus.PENDING

                                        ||

                                        booking.getStatus()
                                                == BookingStatus.ALTERNATIVE_PROPOSED
                        )
                        .count();



        /*
         * Approved bookings belonging to this user.
         */
        long approvedBookings =
                myBookings
                        .stream()
                        .filter(
                                booking ->

                                        booking.getStatus()
                                                == BookingStatus.APPROVED
                        )
                        .count();



        /*
         * Next five approved bookings.
         */
        List<Booking> upcomingBookings =
                myBookings
                        .stream()
                        .filter(
                                booking ->

                                        booking.getStatus()
                                                == BookingStatus.APPROVED

                                        &&

                                        !booking.getDate()
                                                .isBefore(today)
                        )
                        .sorted(

                                Comparator
                                        .comparing(
                                                Booking::getDate
                                        )
                                        .thenComparing(
                                                Booking::getStartTime
                                        )

                        )
                        .limit(5)
                        .toList();



        model.addAttribute(
                "pending",
                pendingBookings
        );


        model.addAttribute(
                "approved",
                approvedBookings
        );


        model.addAttribute(
                "upcoming",
                upcomingBookings
        );



        /*
         * =====================================================
         * LECTURER COURSES
         * =====================================================
         *
         * Only lecturers have assigned courses.
         */
        if (user.getRole() == UserRole.LECTURER) {

            model.addAttribute(

                    "courses",

                    courses.findActiveCoursesForLecturer(
                            user.getId()
                    )

            );

        }



        return "dashboard";
    }

}