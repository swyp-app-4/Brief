package com.brife.notification;

import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.service.NewsService;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.brife.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final String NOTIFICATION_BODY = "지금 추천 뉴스를 확인해보세요.";

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final NewsService newsService;
    private final RecommendedNewsSelector recommendedNewsSelector;
    private final NotificationSettingService notificationSettingService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendAt8am() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime8am());
    }

    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void sendAt12pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime12pm());
    }

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void sendAt6pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime6pm());
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendAt10pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime10pm());
    }

    private void sendToUsers(Predicate<UserNotificationSetting> filter) {
        List<UserNotificationSetting> settings = userNotificationSettingRepository.findAll();
        for (UserNotificationSetting setting : settings) {
            if (!filter.test(setting)) {
                continue;
            }

            Long userId = setting.getUserId();
            try {
                WidgetNewsDto selectedNews = recommendedNewsSelector
                        .select(newsService.getRecommendedNews(userId))
                        .orElse(null);
                if (selectedNews == null) {
                    log.warn("Push notification skipped because no recommendation was found. userId={}", userId);
                    continue;
                }

                notificationSettingService.sendPushNotification(
                        userId,
                        selectedNews.getTitle(),
                        NOTIFICATION_BODY,
                        selectedNews.getId()
                );
                log.info("Push notification sent. userId={}, newsId={}", userId, selectedNews.getId());
            } catch (Exception e) {
                log.error("Push notification failed. userId={}", userId, e);
            }
        }
    }
}
