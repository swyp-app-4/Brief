package com.brife.notification;

import com.brife.notification.entity.UserFcmToken;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;

    // 매일 오전 8시
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendAt8am() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime8am());
    }

    // 매일 낮 12시
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void sendAt12pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime12pm());
    }

    // 매일 오후 6시
    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void sendAt6pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime6pm());
    }

    // 매일 오후 10시
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendAt10pm() {
        sendToUsers(setting -> setting.isDailyNewsEnabled() && setting.isTime10pm());
    }

    private void sendToUsers(java.util.function.Predicate<UserNotificationSetting> filter) {
        List<UserNotificationSetting> settings = userNotificationSettingRepository.findAll();
        for (UserNotificationSetting setting : settings) {
            if (!filter.test(setting)) continue;
            userFcmTokenRepository.findByUserId(setting.getUserId()).ifPresent(fcmToken -> {
                try {
                    Message message = Message.builder()
                            .setNotification(Notification.builder()
                                    .setTitle("브리프 데일리 뉴스")
                                    .setBody("오늘의 추천 뉴스를 확인해보세요!")
                                    .build())
                            .setToken(fcmToken.getFcmToken())
                            .build();
                    FirebaseMessaging.getInstance().send(message);
                    log.info("푸시 알림 전송 성공 userId={}", setting.getUserId());
                } catch (Exception e) {
                    log.error("푸시 알림 전송 실패 userId={}", setting.getUserId(), e);
                }
            });
        }
    }
}