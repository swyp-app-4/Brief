package com.brife.notification;

import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.batch.BatchMetadataHolder;
import com.brife.news.service.NewsService;
import com.brife.notification.dto.TopNewsNotificationItem;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.brife.notification.service.NotificationSettingService;
import com.brife.notification.service.PushDeliveryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final Duration MAX_EXPECTED_BATCH_AGE = Duration.ofHours(4);

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final NewsService newsService;
    private final NotificationSettingService notificationSettingService;
    private final BatchMetadataHolder batchMetadataHolder;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendAt8am() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime8am(), NewsNotificationSlot.MORNING);
    }

    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void sendAt12pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime12pm(), NewsNotificationSlot.LUNCH);
    }

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void sendAt6pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime6pm(), NewsNotificationSlot.EVENING);
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendAt10pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime10pm(), NewsNotificationSlot.NIGHT);
    }

    private void sendToUsers(Predicate<UserNotificationSetting> filter, NewsNotificationSlot slot) {
        logBatchFreshness(slot);
        List<UserNotificationSetting> settings = userNotificationSettingRepository.findAll();
        for (UserNotificationSetting setting : settings) {
            if (!filter.test(setting)) {
                continue;
            }

            Long userId = setting.getUserId();
            try {
                List<TopNewsNotificationItem> newsItems = newsService.getRecommendedNews(userId).stream()
                        .limit(5)
                        .map(this::toNotificationItem)
                        .toList();
                if (newsItems.isEmpty()) {
                    log.warn("Push notification skipped because no recommendation was found. userId={}", userId);
                    continue;
                }

                PushDeliveryResult result = notificationSettingService
                        .sendTop5NewsNotification(userId, slot, newsItems);
                if (result == PushDeliveryResult.SENT) {
                    log.info("Top5 push notification sent. userId={}, slot={}, primaryNewsId={}, newsCount={}",
                            userId, slot, newsItems.getFirst().newsId(), newsItems.size());
                } else if (result == PushDeliveryResult.SKIPPED_NO_TOKEN) {
                    log.debug("Push notification skipped because no FCM token exists. userId={}", userId);
                }
            } catch (Exception e) {
                log.error("Push notification failed. userId={}", userId, e);
            }
        }
    }

    private void logBatchFreshness(NewsNotificationSlot slot) {
        if (batchMetadataHolder.isBatchRunning()) {
            log.warn("News batch is still running at notification time. Existing cached recommendations " +
                            "will remain available until completion. slot={}, startedAt={}",
                    slot, batchMetadataHolder.getLastBatchStartedAt().orElse(null));
        }

        batchMetadataHolder.getLastBatchCompletedAt().ifPresentOrElse(completedAt -> {
            Duration age = Duration.between(completedAt, LocalDateTime.now());
            if (age.compareTo(MAX_EXPECTED_BATCH_AGE) > 0) {
                log.warn("Latest completed news batch is stale at notification time. slot={}, completedAt={}, ageMinutes={}",
                        slot, completedAt, age.toMinutes());
            } else {
                log.info("News notification uses data from a recent completed batch. slot={}, completedAt={}, ageMinutes={}",
                        slot, completedAt, age.toMinutes());
            }
        }, () -> log.warn("Completed news batch metadata is unavailable. The application may have restarted. slot={}", slot));
    }

    private TopNewsNotificationItem toNotificationItem(WidgetNewsDto news) {
        return new TopNewsNotificationItem(news.getId(), news.getTitle());
    }
}
