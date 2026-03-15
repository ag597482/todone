package com.indra.todone.dto.request;

import com.indra.todone.model.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    private String notificationTitle;
    private String notificationDesc;
    private String targetedUser;
    @Builder.Default
    private NotificationStatus notificationStatus = NotificationStatus.DELIVERED;
}
