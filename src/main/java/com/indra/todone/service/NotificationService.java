package com.indra.todone.service;

import com.indra.todone.dto.request.CreateNotificationRequest;
import com.indra.todone.dto.request.UpdateNotificationStatusRequest;
import com.indra.todone.model.Notification;
import com.indra.todone.model.NotificationStatus;
import com.indra.todone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
                .notificationDesc(request.getNotificationDesc())
                .targetedUser(request.getTargetedUser())
                .notificationTime(Instant.now())
                .notificationStatus(status)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getByUserId(String userId) {
        return notificationRepository.findByTargetedUserOrderByNotificationTimeDesc(userId);
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
}
