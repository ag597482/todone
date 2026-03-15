package com.indra.todone.repository;

import com.indra.todone.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByTargetedUserOrderByNotificationTimeDesc(String targetedUser);
}
