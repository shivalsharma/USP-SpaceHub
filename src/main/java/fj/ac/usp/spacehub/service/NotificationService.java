package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.model.*;
import fj.ac.usp.spacehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepo;
    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final SettingsService settings;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    @Value("${app.mail.enabled:false}") private boolean mailEnabled;

    @Transactional
    public void notify(UserAccount user, String title, String message, String dedupKey) {
        if (dedupKey != null && notificationRepo.findByDedupKey(dedupKey).isPresent()) return;
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setDedupKey(dedupKey);
        notificationRepo.save(n);
        if (mailEnabled) {
            try {
                JavaMailSender sender = mailSenderProvider.getIfAvailable();
                if (sender != null) {
                    SimpleMailMessage m = new SimpleMailMessage();
                    m.setTo(user.getEmail());
                    m.setSubject("USP SpaceHub - " + title);
                    m.setText(message);
                    sender.send(m);
                }
            } catch (Exception ignored) {}
        }
    }

    public void notifyAdmins(String title, String message) {
        for (UserAccount u : userRepo.findByRoleInOrderByName(List.of(UserRole.ADMIN))) {
            if (u.isActive()) notify(u, title, message, null);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendUpcomingReminders() {
        int hours = settings.getInt("reminder.hours.before", 24);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Pacific/Fiji"));
        ZonedDateTime until = now.plusHours(hours);
        LocalDate from = now.toLocalDate(), to = until.toLocalDate();
        for (Booking b : bookingRepo.findByDateBetweenAndStatus(from, to, BookingStatus.APPROVED)) {
            LocalDateTime start = LocalDateTime.of(b.getDate(), b.getStartTime());
            LocalDateTime n = now.toLocalDateTime(), u = until.toLocalDateTime();
            if (!start.isBefore(n) && !start.isAfter(u)) {
                notify(b.getRequester(), "Upcoming booking reminder",
                        b.getType() + " in " + b.getRoom().getCode() + " on " + b.getDate() + " from " + b.getStartTime() + " to " + b.getEndTime(),
                        "REMINDER-" + b.getId() + "-" + hours);
            }
        }
    }
}