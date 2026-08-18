package com.brife.user.service;

import com.brife.news.repository.CategoryGroupRepository;
import com.brife.news.repository.CategoryRepository;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.brife.user.domain.AppUser;
import com.brife.user.dto.WithdrawalRequest;
import com.brife.user.exception.InvalidWithdrawalRequestException;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceWithdrawalTest {

    @Test
    void withdrawalMarksUserDeletedAndRevokesAllRefreshTokens() {
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        UserInterestRepository userInterestRepository = mock(UserInterestRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        UserFcmTokenRepository fcmTokenRepository = mock(UserFcmTokenRepository.class);
        NaverUnlinkClient naverUnlinkClient = mock(NaverUnlinkClient.class);
        AppUser user = AppUser.builder().id(1L).email("user@example.com")
                .provider("google").providerId("provider-id").build();
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));

        ProfileService service = new ProfileService(
                appUserRepository,
                userInterestRepository,
                mock(CategoryRepository.class),
                mock(CategoryGroupRepository.class),
                refreshTokenRepository,
                fcmTokenRepository,
                naverUnlinkClient);

        service.deleteUser(1L, null);

        assertThat(user.isDeleted()).isTrue();
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(fcmTokenRepository).deleteByUserId(1L);
    }

    @Test
    void naverWithdrawalRevokesRefreshToken() {
        Fixture fixture = fixtureFor("naver");

        fixture.service.deleteUser(1L, new WithdrawalRequest("naver-refresh-token"));

        assertThat(fixture.user.isDeleted()).isTrue();
        verify(fixture.naverUnlinkClient).revokeRefreshToken("naver-refresh-token");
    }

    @Test
    void naverWithdrawalRequiresRefreshToken() {
        Fixture fixture = fixtureFor("naver");

        assertThatThrownBy(() -> fixture.service.deleteUser(1L, null))
                .isInstanceOf(InvalidWithdrawalRequestException.class);

        assertThat(fixture.user.isDeleted()).isFalse();
    }

    @Test
    void naverRevokeFailureDoesNotCancelBriefWithdrawal() {
        Fixture fixture = fixtureFor("naver");
        doThrow(new RuntimeException("provider unavailable"))
                .when(fixture.naverUnlinkClient).revokeRefreshToken("naver-refresh-token");

        fixture.service.deleteUser(1L, new WithdrawalRequest("naver-refresh-token"));

        assertThat(fixture.user.isDeleted()).isTrue();
    }

    private Fixture fixtureFor(String provider) {
        AppUserRepository users = mock(AppUserRepository.class);
        AppUser user = AppUser.builder().id(1L).email("user@example.com")
                .provider(provider).providerId("provider-id").build();
        when(users.findById(1L)).thenReturn(Optional.of(user));
        NaverUnlinkClient naverUnlinkClient = mock(NaverUnlinkClient.class);
        ProfileService service = new ProfileService(
                users,
                mock(UserInterestRepository.class),
                mock(CategoryRepository.class),
                mock(CategoryGroupRepository.class),
                mock(RefreshTokenRepository.class),
                mock(UserFcmTokenRepository.class),
                naverUnlinkClient);
        return new Fixture(service, user, naverUnlinkClient);
    }

    private record Fixture(ProfileService service, AppUser user, NaverUnlinkClient naverUnlinkClient) {
    }
}
