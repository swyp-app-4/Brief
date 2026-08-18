package com.brife.user.service;

import com.brife.user.domain.AppUser;
import com.brife.user.domain.RefreshToken;
import com.brife.user.exception.AccountWithdrawnException;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.security.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthServiceWithdrawalTest {

    @Test
    void withdrawnUserCannotReissueAccessToken() {
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L).token("refresh-token").userId(7L)
                .expiresAt(LocalDateTime.now().plusDays(1)).build();
        AppUser withdrawnUser = AppUser.builder().id(7L).email("user@example.com")
                .provider("google").providerId("provider-id")
                .deletedAt(LocalDateTime.now()).build();
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(withdrawnUser));

        OAuthService service = new OAuthService(appUserRepository, refreshTokenRepository,
                mock(JwtProvider.class), new ObjectMapper());

        assertThatThrownBy(() -> service.refresh("refresh-token"))
                .isInstanceOf(AccountWithdrawnException.class);
        verify(refreshTokenRepository).delete(refreshToken);
    }
}
