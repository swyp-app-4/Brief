// [레포지토리] UserNotificationSetting JPA 레포지토리. userId 기반 단건 조회.
package com.brife.user.repository;

import com.brife.user.domain.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
    Optional<UserNotificationSetting> findByUserId(Long userId);
}
