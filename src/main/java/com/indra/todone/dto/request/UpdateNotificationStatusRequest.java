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
public class UpdateNotificationStatusRequest {

    private String userId;
    private NotificationStatus notificationStatus;
}
