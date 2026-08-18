package com.brife.user.service;

import com.brife.archive.repository.ArchiveItemRepository;
import com.brife.archive.repository.ArchiveRepository;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpiredUserDeletionService {

    private final AppUserRepository appUserRepository;
    private final UserInterestRepository userInterestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ArchiveItemRepository archiveItemRepository;
    private final ArchiveRepository archiveRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteByUserId(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            return false;
        }

        archiveItemRepository.deleteByUserId(userId);
        archiveRepository.deleteByUserId(userId);
        userInterestRepository.deleteByUserId(userId);
        refreshTokenRepository.deleteByUserId(userId);
        userFcmTokenRepository.deleteByUserId(userId);
        userNotificationSettingRepository.deleteByUserId(userId);
        appUserRepository.deleteById(userId);
        return true;
    }
}
