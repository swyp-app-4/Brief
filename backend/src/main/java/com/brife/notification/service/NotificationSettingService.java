package com.brife.notification.service;

import com.brife.notification.dto.request.NotificationSettingRequest;
import com.brife.notification.dto.response.NotificationSettingResponse;
import com.brife.notification.NewsNotificationSlot;
import com.brife.notification.dto.TopNewsNotificationItem;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserNotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.brife.notification.dto.request.FcmTokenRequest;
import com.brife.notification.entity.UserFcmToken;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationSettingService {

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final TopNewsNotificationPayloadFactory topNewsNotificationPayloadFactory;
    private final FirebaseMessageSender firebaseMessageSender;

    @Transactional(readOnly = true)
    public NotificationSettingResponse getSettings(Long userId) {
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> new UserNotificationSetting(userId));
        return new NotificationSettingResponse(setting);
    }

    public NotificationSettingResponse updateSettings(Long userId, NotificationSettingRequest request) {
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> userNotificationSettingRepository.save(new UserNotificationSetting(userId)));
        setting.update(
                request.isDailyNewsEnabled(),
                request.isTime8am(),
                request.isTime12pm(),
                request.isTime6pm(),
                request.isTime10pm()
        );
        return new NotificationSettingResponse(userNotificationSettingRepository.save(setting));
    }

    public void registerFcmToken(Long userId, FcmTokenRequest request) {
        UserFcmToken fcmToken = userFcmTokenRepository.findByUserId(userId)
                .orElseGet(() -> new UserFcmToken(userId, request.getFcmToken()));
        fcmToken.updateToken(request.getFcmToken());
        userFcmTokenRepository.save(fcmToken);
    }

    public void sendPushNotification(Long userId, String title, String body, Long newsId) {
        UserFcmToken fcmToken = userFcmTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("FCM 토큰이 없습니다."));

        Message.Builder messageBuilder = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setToken(fcmToken.getFcmToken());

        if (newsId != null) {
            messageBuilder.putData("newsId", String.valueOf(newsId));
        }

        deliver(userId, fcmToken.getFcmToken(), messageBuilder.build());
    }

    public PushDeliveryResult sendTop5NewsNotification(Long userId,
                                                       NewsNotificationSlot slot,
                                                       List<TopNewsNotificationItem> newsItems) {
        UserFcmToken fcmToken = userFcmTokenRepository.findByUserId(userId).orElse(null);
        if (fcmToken == null) {
            return PushDeliveryResult.SKIPPED_NO_TOKEN;
        }
        Map<String, String> payload = topNewsNotificationPayloadFactory.create(slot, newsItems);

        Message message = Message.builder()
                .setToken(fcmToken.getFcmToken())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .putAllData(payload)
                .build();

        return deliver(userId, fcmToken.getFcmToken(), message);
    }

    private PushDeliveryResult deliver(Long userId, String failedToken, Message message) {
        try {
            firebaseMessageSender.send(message);
            return PushDeliveryResult.SENT;
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                int deleted = userFcmTokenRepository.deleteByUserIdAndFcmToken(userId, failedToken);
                log.warn("Invalid FCM token handled. userId={}, removed={}", userId, deleted == 1);
                return PushDeliveryResult.INVALID_TOKEN_REMOVED;
            }
            throw new RuntimeException("푸시 알림 전송 실패: " + e.getMessage(), e);
        }
    }
}
