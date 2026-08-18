package com.brife.notification.repository;

import com.brife.notification.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
    Optional<UserNotificationSetting> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
