package com.indra.todone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.todone.dto.request.CreateNotificationRequest;
import com.indra.todone.dto.request.UpdateNotificationStatusRequest;
import com.indra.todone.dto.response.NotificationBodyResponse;
import com.indra.todone.model.Notification;
import com.indra.todone.model.NotificationStatus;
import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStatus;
import com.indra.todone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final NotificationRepository notificationRepository;
    private final TaskService taskService;
    private final OpenAIService openAIService;

    public Notification create(CreateNotificationRequest request) {
        if (request.getTargetedUser() == null || request.getTargetedUser().isBlank()) {
            throw new IllegalArgumentException("targetedUser is required.");
        }
        NotificationStatus status = request.getNotificationStatus() != null
                ? request.getNotificationStatus()
                : NotificationStatus.DELIVERED;
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .notificationTitle(request.getNotificationTitle())
                .notificationDesc(normalizeNotificationDesc(request.getNotificationDesc()))
                .targetedUser(request.getTargetedUser())
                .notificationTime(Instant.now())
                .notificationStatus(status)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getByUserId(String userId) {
        List<Notification> notifications = notificationRepository.findByTargetedUserOrderByNotificationTimeDesc(userId);
        for (Notification n : notifications) {
            n.setNotificationDesc(normalizeNotificationDesc(n.getNotificationDesc()));
        }
        return notifications;
    }

    /**
     * Deletes all READ notifications for the given user.
     */
    public void deleteAllReadByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        notificationRepository.deleteByTargetedUserAndNotificationStatus(userId, NotificationStatus.READ);
    }

    /**
     * Updates the notification status. Only the targeted user can update. Returns empty if notification not found or user not the target.
     */
    public Optional<Notification> updateStatus(String notificationId, UpdateNotificationStatusRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (request.getNotificationStatus() == null) {
            throw new IllegalArgumentException("notificationStatus is required.");
        }
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getTargetedUser().equals(request.getUserId()))
                .map(n -> {
                    n.setNotificationStatus(request.getNotificationStatus());
                    return notificationRepository.save(n);
                });
    }

    /**
     * Returns the list of tasks used for generating the notification body: today's pending tasks for the user.
     */
    public List<Task> getTasksForNotificationBody(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        List<Task> allForToday = taskService.getTasksForUserId(userId, Optional.of(LocalDate.now()));
        return allForToday.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * Generates a notification title and body for the user's pending tasks for today, using OpenAI.
     */
    public NotificationBodyResponse generateNotificationBody(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        List<Task> pendingToday = getTasksForNotificationBody(userId);
        String tasksJson = toTasksJson(pendingToday);
        return openAIService.generateNotificationBody(tasksJson);
    }

    /**
     * Serializes tasks to JSON using only plain, serializable fields to avoid BSON/custom types from MongoDB breaking the payload.
     */
    private String toTasksJson(List<Task> tasks) {
        List<Map<String, Object>> plain = tasks.stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("task_id", t.getTaskId());
                    m.put("name", t.getName());
                    m.put("description", t.getDescription());
                    m.put("dueDate", t.getDueDate() != null ? t.getDueDate().toString() : null);
                    m.put("doneDate", t.getDoneDate() != null ? t.getDoneDate().toString() : null);
                    m.put("status", t.getStatus() != null ? t.getStatus().name() : null);
                    m.put("authorId", t.getAuthorId());
                    return m;
                })
                .collect(Collectors.toList());
        try {
            return OBJECT_MAPPER.writeValueAsString(plain);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * Normalizes the notification description so we never store fenced JSON payloads like:
     * ```json
     * { "title": "...", "body": "..." }
     * ```
     * If a fenced JSON object with a "body" field is detected, that "body" string is stored instead.
     */
    private String normalizeNotificationDesc(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith("```") || !trimmed.endsWith("```")) {
            return raw;
        }

        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline < 0) {
            return raw;
        }

        // Strip ``` or ```json line
        String withoutFenceLine = trimmed.substring(firstNewline + 1);
        int lastFence = withoutFenceLine.lastIndexOf("```");
        if (lastFence < 0) {
            return raw;
        }
        String inner = withoutFenceLine.substring(0, lastFence).trim();
        if (inner.isEmpty()) {
            return raw;
        }

        try {
            var node = OBJECT_MAPPER.readTree(inner);
            if (node.has("body")) {
                return node.get("body").asText();
            }
            return inner;
        } catch (Exception e) {
            return inner;
        }
    }
}
