package com.indra.todone.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @Field("notification_id")
    @JsonProperty("notification_id")
    private String notificationId;
    @Field("notification_title")
    @JsonProperty("notification_title")
    private String notificationTitle;
    @Field("notification_desc")
    @JsonProperty("notification_desc")
    private String notificationDesc;
    @Field("targeted_user")
    @JsonProperty("targeted_user")
    private String targetedUser;
    @Field("notification_time")
    @JsonProperty("notification_time")
    private Instant notificationTime;
    @Field("notification_status")
    @JsonProperty("notification_status")
    private NotificationStatus notificationStatus;
}
