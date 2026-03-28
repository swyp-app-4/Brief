package com.brife.user.scheduler;

import com.brife.user.domain.AppUser;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final AppUserRepository appUserRepository;
    private final UserInterestRepository userInterestRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void hardDeleteExpiredUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<AppUser> expiredUsers = appUserRepository.findByDeletedAtIsNotNullAndDeletedAtBefore(cutoff);

        for (AppUser user : expiredUsers) {
            userInterestRepository.deleteByUserId(user.getId());
            refreshTokenRepository.deleteByUserId(user.getId());
            appUserRepository.delete(user);
        }
    }
}
