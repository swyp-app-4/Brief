package com.brife.user.scheduler;

import com.brife.user.repository.AppUserRepository;
import com.brife.user.service.ExpiredUserDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

    private static final int BATCH_SIZE = 100;

    private final AppUserRepository appUserRepository;
    private final ExpiredUserDeletionService deletionService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void hardDeleteExpiredUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        long lastId = 0L;
        int deletedCount = 0;
        int failedCount = 0;

        while (true) {
            List<Long> userIds = appUserRepository.findExpiredUserIds(
                    cutoff, lastId, PageRequest.of(0, BATCH_SIZE));
            if (userIds.isEmpty()) {
                break;
            }

            for (Long userId : userIds) {
                lastId = userId;
                try {
                    if (deletionService.deleteByUserId(userId)) {
                        deletedCount++;
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("탈퇴 사용자 완전 삭제 실패: userId={}", userId, e);
                }
            }
        }

        log.info("탈퇴 사용자 완전 삭제 완료: deletedCount={}, failedCount={}", deletedCount, failedCount);
    }
}
