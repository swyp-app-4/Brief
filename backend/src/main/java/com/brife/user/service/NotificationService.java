// [알림 서비스] 알림 설정 조회 (없으면 기본값 자동 생성) 및 변경.
package com.brife.user.service;

import com.brife.user.domain.AppUser;
import com.brife.user.domain.UserNotificationSetting;
import com.brife.user.dto.NotificationSettingRequest;
import com.brife.user.dto.NotificationSettingResponse;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.UserNotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AppUserRepository appUserRepository;
    private final UserNotificationSettingRepository notificationSettingRepository;

    @Transactional
    public NotificationSettingResponse getNotificationSetting(Long userId) {
        return new NotificationSettingResponse(
                notificationSettingRepository.findByUserId(userId)
                        .orElseGet(() -> createDefaultSetting(userId))
        );
    }

    @Transactional
    public NotificationSettingResponse updateNotificationSetting(Long userId, NotificationSettingRequest request) {
        UserNotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSetting(userId));

        setting.update(
                request.isNotificationEnabled(),
                request.isMorningEnabled(),
                request.isLunchEnabled(),
                request.isEveningEnabled(),
                request.isNightEnabled()
        );

        return new NotificationSettingResponse(notificationSettingRepository.save(setting));
    }

    private UserNotificationSetting createDefaultSetting(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return notificationSettingRepository.save(
                UserNotificationSetting.builder().user(user).build()
        );
    }
}
