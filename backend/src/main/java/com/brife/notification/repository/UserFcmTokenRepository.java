package com.brife.notification.repository;

import com.brife.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    Optional<UserFcmToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserFcmToken token WHERE token.userId = :userId AND token.fcmToken = :fcmToken")
    int deleteByUserIdAndFcmToken(@Param("userId") Long userId, @Param("fcmToken") String fcmToken);
}
