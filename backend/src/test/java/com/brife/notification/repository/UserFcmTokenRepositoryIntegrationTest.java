package com.brife.notification.repository;

import com.brife.notification.entity.UserFcmToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
class UserFcmTokenRepositoryIntegrationTest {

    @Autowired
    private UserFcmTokenRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void doesNotDeleteReplacementTokenWhenOldTokenFailureArrivesLate() {
        long userId = -System.nanoTime();
        repository.saveAndFlush(new UserFcmToken(userId, "old-token"));
        jdbcTemplate.update(
                "UPDATE user_fcm_token SET fcm_token = ? WHERE user_id = ?",
                "replacement-token",
                userId);

        int deleted = repository.deleteByUserIdAndFcmToken(userId, "old-token");

        assertThat(deleted).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT fcm_token FROM user_fcm_token WHERE user_id = ?",
                String.class,
                userId)).isEqualTo("replacement-token");
    }
}
