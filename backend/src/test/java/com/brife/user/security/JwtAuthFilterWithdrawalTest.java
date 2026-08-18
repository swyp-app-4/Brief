package com.brife.user.security;

import com.brife.user.domain.AppUser;
import com.brife.user.repository.AppUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthFilterWithdrawalTest {

    @Test
    void withdrawnUserAccessTokenIsRejectedWithDedicatedError() throws Exception {
        JwtProvider jwtProvider = mock(JwtProvider.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        Claims claims = mock(Claims.class);
        when(jwtProvider.isValid("access-token")).thenReturn(true);
        when(jwtProvider.parseToken("access-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("7");
        when(claims.get("role", String.class)).thenReturn("ROLE_USER");
        AppUser user = AppUser.builder().id(7L).email("user@example.com")
                .provider("google").providerId("provider-id")
                .deletedAt(LocalDateTime.now()).build();
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        new JwtAuthFilter(jwtProvider, appUserRepository).doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("ACCOUNT_WITHDRAWN");
        verify(chain, never()).doFilter(request, response);
    }
}
