package com.brife.notification;

import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.service.NewsService;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.brife.notification.service.NotificationSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private UserNotificationSettingRepository settingRepository;

    @Mock
    private NewsService newsService;

    @Mock
    private RecommendedNewsSelector recommendedNewsSelector;

    @Mock
    private NotificationSettingService notificationSettingService;

    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationScheduler(
                settingRepository,
                newsService,
                recommendedNewsSelector,
                notificationSettingService
        );
    }

    @Test
    void sendsSelectedNewsTitleAndIdAtEnabledTime() {
        UserNotificationSetting setting = enabledAt8am(7L);
        WidgetNewsDto selected = WidgetNewsDto.builder().id(31L).title("추천 기사 제목").build();
        List<WidgetNewsDto> recommendations = List.of(selected);
        when(settingRepository.findAll()).thenReturn(List.of(setting));
        when(newsService.getRecommendedNews(7L)).thenReturn(recommendations);
        when(recommendedNewsSelector.select(recommendations)).thenReturn(Optional.of(selected));

        scheduler.sendAt8am();

        verify(notificationSettingService).sendPushNotification(
                7L,
                "추천 기사 제목",
                "지금 추천 뉴스를 확인해보세요.",
                31L
        );
    }

    @Test
    void skipsPushWhenRecommendationIsEmpty() {
        UserNotificationSetting setting = enabledAt8am(7L);
        when(settingRepository.findAll()).thenReturn(List.of(setting));
        when(newsService.getRecommendedNews(7L)).thenReturn(List.of());
        when(recommendedNewsSelector.select(List.of())).thenReturn(Optional.empty());

        scheduler.sendAt8am();

        verify(notificationSettingService, never())
                .sendPushNotification(org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void continuesWithNextUserWhenOnePushFails() {
        UserNotificationSetting firstSetting = enabledAt8am(7L);
        UserNotificationSetting secondSetting = enabledAt8am(8L);
        WidgetNewsDto firstNews = WidgetNewsDto.builder().id(31L).title("첫 기사").build();
        WidgetNewsDto secondNews = WidgetNewsDto.builder().id(32L).title("둘째 기사").build();
        List<WidgetNewsDto> firstRecommendations = List.of(firstNews);
        List<WidgetNewsDto> secondRecommendations = List.of(secondNews);

        when(settingRepository.findAll()).thenReturn(List.of(firstSetting, secondSetting));
        when(newsService.getRecommendedNews(7L)).thenReturn(firstRecommendations);
        when(newsService.getRecommendedNews(8L)).thenReturn(secondRecommendations);
        when(recommendedNewsSelector.select(firstRecommendations)).thenReturn(Optional.of(firstNews));
        when(recommendedNewsSelector.select(secondRecommendations)).thenReturn(Optional.of(secondNews));
        doThrow(new RuntimeException("FCM failure"))
                .when(notificationSettingService)
                .sendPushNotification(7L, "첫 기사", "지금 추천 뉴스를 확인해보세요.", 31L);

        scheduler.sendAt8am();

        verify(notificationSettingService)
                .sendPushNotification(8L, "둘째 기사", "지금 추천 뉴스를 확인해보세요.", 32L);
    }

    private UserNotificationSetting enabledAt8am(Long userId) {
        UserNotificationSetting setting = new UserNotificationSetting(userId);
        setting.update(true, true, false, false, false);
        return setting;
    }
}
