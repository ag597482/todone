package com.indra.todone.service;

import com.indra.todone.dto.request.CreateNotificationRequest;
import com.indra.todone.dto.response.NotificationBodyResponse;
import com.indra.todone.model.Task;
import com.indra.todone.model.User;
import com.indra.todone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerService {

    private static final String TODONE_FRONTEND_URL = "https://ag597482.github.io/todone_frontend";

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TelegramService telegramService;

    /**
     * Runs the scheduled notification job: for each user with pending tasks (today), generates a notification body,
     * creates the notification, and sends it via Telegram if the user has Telegram linked.
     * Skips creation and sending when the user has no pending tasks.
     */
    public void runScheduledNotificationJob() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                List<Task> pendingToday = notificationService.getTasksForNotificationBody(user.getUserId());
                if (pendingToday.isEmpty()) {
                    continue;
                }

                NotificationBodyResponse response = notificationService.generateNotificationBody(user.getUserId());
                String title = response.getTitle() != null ? response.getTitle() : "Tasks reminder";
                String body = response.getBody() != null ? response.getBody() : "";

                notificationService.create(CreateNotificationRequest.builder()
                        .notificationTitle(title)
                        .notificationDesc(body)
                        .targetedUser(user.getUserId())
                        .build());

                if (telegramService.hasTelegramLinked(user)) {
                    String message = body.isBlank() ? title : title + "\n\n" + body;
                    message = message + "\n\n" + TODONE_FRONTEND_URL;
                    telegramService.sendMessageToUser(user, message);
                }
            } catch (Exception e) {
                log.warn("Scheduled notification failed for user {}: {}", user.getUserId(), e.getMessage());
            }
        }
    }
}
