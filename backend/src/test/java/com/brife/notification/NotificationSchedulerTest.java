package com.brife.notification;

import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.batch.BatchMetadataHolder;
import com.brife.news.service.NewsService;
import com.brife.notification.dto.TopNewsNotificationItem;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.brife.notification.service.NotificationSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock private UserNotificationSettingRepository settingRepository;
    @Mock private NewsService newsService;
    @Mock private NotificationSettingService notificationSettingService;
    @Mock private BatchMetadataHolder batchMetadataHolder;

    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationScheduler(
                settingRepository, newsService, notificationSettingService, batchMetadataHolder);
    }

    @Test
    void sendsOrderedTop5NewsAtEnabledTime() {
        UserNotificationSetting setting = enabledAt8am(7L);
        List<WidgetNewsDto> recommendations = List.of(
                news(31L, "첫 번째"), news(32L, "두 번째"), news(33L, "세 번째"),
                news(34L, "네 번째"), news(35L, "다섯 번째"), news(36L, "여섯 번째")
        );
        when(settingRepository.findAll()).thenReturn(List.of(setting));
        when(newsService.getRecommendedNews(7L)).thenReturn(recommendations);

        scheduler.sendAt8am();

        verify(notificationSettingService).sendTop5NewsNotification(
                7L,
                NewsNotificationSlot.MORNING,
                List.of(
                        new TopNewsNotificationItem(31L, "첫 번째"),
                        new TopNewsNotificationItem(32L, "두 번째"),
                        new TopNewsNotificationItem(33L, "세 번째"),
                        new TopNewsNotificationItem(34L, "네 번째"),
                        new TopNewsNotificationItem(35L, "다섯 번째")
                )
        );
    }

    @Test
    void usesLunchSlotAtNoon() {
        UserNotificationSetting setting = new UserNotificationSetting(7L);
        setting.update(true, false, true, false, false);
        when(settingRepository.findAll()).thenReturn(List.of(setting));
        when(newsService.getRecommendedNews(7L)).thenReturn(List.of(news(31L, "점심 뉴스")));

        scheduler.sendAt12pm();

        verify(notificationSettingService).sendTop5NewsNotification(
                7L,
                NewsNotificationSlot.LUNCH,
                List.of(new TopNewsNotificationItem(31L, "점심 뉴스"))
        );
    }

    @Test
    void skipsPushWhenRecommendationIsEmpty() {
        UserNotificationSetting setting = enabledAt8am(7L);
        when(settingRepository.findAll()).thenReturn(List.of(setting));
        when(newsService.getRecommendedNews(7L)).thenReturn(List.of());

        scheduler.sendAt8am();

        verify(notificationSettingService, never())
                .sendTop5NewsNotification(anyLong(), any(), any());
    }

    @Test
    void continuesWithNextUserWhenOnePushFails() {
        UserNotificationSetting firstSetting = enabledAt8am(7L);
        UserNotificationSetting secondSetting = enabledAt8am(8L);
        List<TopNewsNotificationItem> firstItems = List.of(new TopNewsNotificationItem(31L, "첫 기사"));
        List<TopNewsNotificationItem> secondItems = List.of(new TopNewsNotificationItem(32L, "둘째 기사"));

        when(settingRepository.findAll()).thenReturn(List.of(firstSetting, secondSetting));
        when(newsService.getRecommendedNews(7L)).thenReturn(List.of(news(31L, "첫 기사")));
        when(newsService.getRecommendedNews(8L)).thenReturn(List.of(news(32L, "둘째 기사")));
        doThrow(new RuntimeException("FCM failure"))
                .when(notificationSettingService)
                .sendTop5NewsNotification(7L, NewsNotificationSlot.MORNING, firstItems);

        scheduler.sendAt8am();

        verify(notificationSettingService)
                .sendTop5NewsNotification(8L, NewsNotificationSlot.MORNING, secondItems);
    }

    private WidgetNewsDto news(Long id, String title) {
        return WidgetNewsDto.builder().id(id).title(title).build();
    }

    private UserNotificationSetting enabledAt8am(Long userId) {
        UserNotificationSetting setting = new UserNotificationSetting(userId);
        setting.update(true, true, false, false, false);
        return setting;
    }
}
