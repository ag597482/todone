package com.indra.todone.controller;

import com.indra.todone.dto.request.CreateNotificationRequest;
import com.indra.todone.dto.request.UpdateNotificationStatusRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.dto.response.NotificationBodyResponse;
import com.indra.todone.model.Notification;
import com.indra.todone.model.Task;
import com.indra.todone.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Creates a new notification. Body: notificationTitle, notificationDesc, targetedUser. Optional: notificationStatus (default DELIVERED). Returns the created notification.")
    public ResponseEntity<ApiResponse<Notification>> createNotification(@RequestBody CreateNotificationRequest request) {
        if (request.getTargetedUser() == null || request.getTargetedUser().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("targetedUser is required."));
        }
        Notification notification = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification created", notification));
    }

    @GetMapping("/tasks-for-notification")
    @Operation(summary = "Get tasks for notification", description = "Returns the list of today's pending tasks for the user (same list used when generating the notification body). Query param: userId. Uses today's date automatically.")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksForNotification(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("userId is required."));
        }
        List<Task> tasks = notificationService.getTasksForNotificationBody(userId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/generate-body")
    @Operation(summary = "Generate notification body", description = "Generates a notification title and body for the user's pending tasks for today using OpenAI. Query param: userId. Returns { title, body }.")
    public ResponseEntity<ApiResponse<NotificationBodyResponse>> generateNotificationBody(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("userId is required."));
        }
        NotificationBodyResponse body = notificationService.generateNotificationBody(userId);
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for user", description = "Returns all notifications for the given user id, ordered by notification time descending. Returns empty list if none.")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsForUser(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Delete read notifications for user", description = "Deletes all READ notifications for the given user id. DELIVERED notifications are not deleted.")
    public ResponseEntity<ApiResponse<Void>> deleteAllReadByUserId(@PathVariable String userId) {
        notificationService.deleteAllReadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("All read notifications deleted for user"));
    }

    @PatchMapping("/{notificationId}/status")
    @Operation(summary = "Update notification status", description = "Updates the notification status. Path: notificationId. Body: userId, notificationStatus (DELIVERED/READ). Only the targeted user can update. 404 if notification not found or user is not the target.")
    public ResponseEntity<ApiResponse<Notification>> updateNotificationStatus(
            @PathVariable String notificationId,
            @RequestBody UpdateNotificationStatusRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("userId is required."));
        }
        if (request.getNotificationStatus() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("notificationStatus is required."));
        }
        Optional<Notification> updated = notificationService.updateStatus(notificationId, request);
        return updated
                .map(n -> ResponseEntity.ok(ApiResponse.success("Status updated", n)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Notification not found or you are not the targeted user.")));
    }
}
