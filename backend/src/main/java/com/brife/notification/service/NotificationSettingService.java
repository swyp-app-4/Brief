package com.brife.notification.service;

import com.brife.notification.dto.request.NotificationSettingRequest;
import com.brife.notification.dto.response.NotificationSettingResponse;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserNotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingService {

    private final UserNotificationSettingRepository userNotificationSettingRepository;

    @Transactional(readOnly = true)
    public NotificationSettingResponse getSettings(Long userId) {
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> userNotificationSettingRepository.save(new UserNotificationSetting(userId)));
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
}