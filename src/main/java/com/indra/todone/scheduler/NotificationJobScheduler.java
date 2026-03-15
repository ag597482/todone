package com.indra.todone.scheduler;

import com.indra.todone.service.NotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduler.jobs.notification", name = "enabled", havingValue = "true")
public class NotificationJobScheduler {

    private final NotificationSchedulerService notificationSchedulerService;

    @Scheduled(fixedDelayString = "${scheduler.jobs.notification.interval-ms:7200000}")
    public void run() {
        notificationSchedulerService.runScheduledNotificationJob();
    }
}
