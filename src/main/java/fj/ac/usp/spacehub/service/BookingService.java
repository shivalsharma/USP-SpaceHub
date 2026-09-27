package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.dto.*;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final CourseRepository courses;
    private final BookingValidationService validation;
    private final AlternativeSuggestionRepository alternatives;
    private final NotificationService notifications;
    private final AuditService audit;

    @Transactional
    public Booking submit(UserAccount user, Long roomId, SearchForm f, double searchScore) {
        Room room = rooms.findById(roomId).orElseThrow();
        Course course = f.getCourseId() == null ? null : courses.findById(f.getCourseId()).orElseThrow();
        Set<String> req = FacilityUtil.parseCsv(f.getRequiredFacilitiesCsv());
        BookingCandidate c = new BookingCandidate(user, course, room, f.getBookingType(), f.getDate(), f.getStartTime(), f.getEndTime(), f.getExpectedStudents(), req, null);
        ValidationResult vr = validation.validate(c, true);
        if (!vr.valid()) throw new IllegalArgumentException(String.join(" ", vr.violations()));
        Booking b = new Booking();
        b.setRequester(user);
        b.setCourse(course);
        b.setRoom(room);
        b.setType(f.getBookingType());
        b.setDate(f.getDate());
        b.setStartTime(f.getStartTime());
        b.setEndTime(f.getEndTime());
        b.setExpectedStudents(f.getExpectedStudents());
        b.setPurpose(course == null ? f.getBookingType() + " booking" : course.getCode() + " " + f.getBookingType());
        b.setRequiredFacilities(validation.combinedRequirements(course, req));
        b.setStatus(BookingStatus.PENDING);
        b.setSearchScore(searchScore);
        b.setRecommendationReason("Submitted after re-validating all hard constraints.");
        bookings.save(b);
        notifications.notify(user, "Booking request submitted", "Your request for " + room.getCode() + " on " + b.getDate() + " is pending approval.", "SUBMITTED-" + b.getId());
        notifications.notifyAdmins("New booking request", user.getName() + " submitted a " + b.getType() + " request for " + room.getCode() + " on " + b.getDate() + ".");
        audit.log("BOOKING_REQUEST_CREATED", "Booking", b.getId(), b.getRecommendationReason());
        return b;
    }

    @Transactional
    public void cancel(UserAccount user, Long id) {
        Booking b = bookings.findById(id).orElseThrow();
        if (!b.getRequester().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN)
            throw new IllegalArgumentException("You cannot cancel this booking.");
        if (b.getStatus() == BookingStatus.REJECTED || b.getStatus() == BookingStatus.CANCELLED)
            throw new IllegalArgumentException("Booking is already closed.");
        b.setStatus(BookingStatus.CANCELLED);
        b.setAdminDecisionReason("Cancelled by " + user.getEmail());
        bookings.save(b);
        if (!b.getRequester().getId().equals(user.getId()))
            notifications.notify(b.getRequester(), "Booking cancelled by administrator", "Booking #" + b.getId() + " for " + b.getRoom().getCode() + " on " + b.getDate() + " was cancelled.", "ADMIN-CANCEL-" + b.getId());
        notifications.notifyAdmins("Booking cancelled", "Booking #" + b.getId() + " was cancelled by " + user.getName() + ".");
        audit.log("BOOKING_CANCELLED", "Booking", b.getId(), b.getAdminDecisionReason());
    }

    @Transactional
    public void acceptAlternative(UserAccount user, Long suggestionId) {
        AlternativeSuggestion s = alternatives.findById(suggestionId).orElseThrow();
        Booking b = s.getBooking();
        rooms.findByIdForUpdate(s.getRoom().getId()).orElseThrow();
        if (!b.getRequester().getId().equals(user.getId()))
            throw new IllegalArgumentException("This alternative does not belong to you.");
        if (s.getStatus() != AlternativeStatus.PROPOSED)
            throw new IllegalArgumentException("Alternative is no longer active.");
        BookingCandidate c = new BookingCandidate(user, b.getCourse(), s.getRoom(), b.getType(), s.getDate(), s.getStartTime(), s.getEndTime(), b.getExpectedStudents(), b.getRequiredFacilities(), b.getId());
        ValidationResult vr = validation.validate(c, false);
        if (!vr.valid()) throw new IllegalArgumentException("Alternative is no longer valid: " + String.join(" ", vr.violations()));
        b.setRoom(s.getRoom());
        b.setDate(s.getDate());
        b.setStartTime(s.getStartTime());
        b.setEndTime(s.getEndTime());
        b.setStatus(s.isPreserveOriginalOnReject() ? BookingStatus.APPROVED : BookingStatus.PENDING);
        b.setAdminDecisionReason(s.isPreserveOriginalOnReject() ? "Administrator-proposed reallocation accepted; approval retained." : "Alternative accepted by requester and returned to pending approval.");
        bookings.save(b);
        s.setStatus(AlternativeStatus.ACCEPTED);
        alternatives.save(s);
        notifications.notifyAdmins("Alternative accepted", user.getName() + " accepted the alternative for booking #" + b.getId() + ".");
        audit.log("ALTERNATIVE_ACCEPTED", "Booking", b.getId(), s.getExplanation());
    }

    @Transactional
    public void rejectAlternative(UserAccount user, Long suggestionId) {
        AlternativeSuggestion s = alternatives.findById(suggestionId).orElseThrow();
        Booking b = s.getBooking();
        rooms.findByIdForUpdate(s.getRoom().getId()).orElseThrow();
        if (!b.getRequester().getId().equals(user.getId()))
            throw new IllegalArgumentException("This alternative does not belong to you.");
        s.setStatus(AlternativeStatus.REJECTED);
        alternatives.save(s);
        if (s.isPreserveOriginalOnReject()) {
            b.setStatus(BookingStatus.APPROVED);
            b.setAdminDecisionReason("Administrator-proposed reallocation rejected; original approved booking retained.");
        } else {
            b.setStatus(BookingStatus.REJECTED);
            b.setAdminDecisionReason("Alternative rejected by requester.");
        }
        bookings.save(b);
        audit.log("ALTERNATIVE_REJECTED", "Booking", b.getId(), s.getExplanation());
    }
}