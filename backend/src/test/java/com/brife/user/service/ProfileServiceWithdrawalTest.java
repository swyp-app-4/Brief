package com.brife.user.service;

import com.brife.news.repository.CategoryGroupRepository;
import com.brife.news.repository.CategoryRepository;
import com.brife.user.domain.AppUser;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceWithdrawalTest {

    @Test
    void withdrawalMarksUserDeletedAndRevokesAllRefreshTokens() {
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        UserInterestRepository userInterestRepository = mock(UserInterestRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        AppUser user = AppUser.builder().id(1L).email("user@example.com")
                .provider("google").providerId("provider-id").build();
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));

        ProfileService service = new ProfileService(
                appUserRepository,
                userInterestRepository,
                mock(CategoryRepository.class),
                mock(CategoryGroupRepository.class),
                refreshTokenRepository);

        service.deleteUser(1L);

        assertThat(user.isDeleted()).isTrue();
        verify(refreshTokenRepository).deleteByUserId(1L);
    }
}
