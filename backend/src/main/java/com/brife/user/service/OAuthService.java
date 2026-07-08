package com.brife.user.service;

import com.brife.user.domain.AppUser;
import com.brife.user.domain.RefreshToken;
import com.brife.user.dto.AuthResponse;
import com.brife.user.dto.TermsRequest;
import com.brife.user.exception.AuthException;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.security.JwtProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @SuppressWarnings("unchecked")
    @Transactional
    public AuthResponse loginWithKakao(String accessToken) {
        Map<String, Object> userInfo = RestClient.create()
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String providerId = String.valueOf(userInfo.get("id"));
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        return generateAuthResponse(email, nickname, "kakao", providerId);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public AuthResponse loginWithNaver(String accessToken) {
        Map<String, Object> userInfo = RestClient.create()
                .get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        Map<String, Object> response = (Map<String, Object>) userInfo.get("response");

        String providerId = (String) response.get("id");
        String email = (String) response.get("email");
        String nickname = (String) response.get("nickname");

        return generateAuthResponse(email, nickname, "naver", providerId);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        try {
            String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<>() {});

            String providerId = (String) claims.get("sub");
            String email = (String) claims.get("email");
            String nickname = (String) claims.get("name");

            return generateAuthResponse(email, nickname, "google", providerId);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 Google ID Token입니다.");
        }
    }

    @Transactional
    public String refresh(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new AuthException("Refresh Token이 필요합니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthException("유효하지 않은 Refresh Token입니다."));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("만료된 Refresh Token입니다. 다시 로그인해주세요.");
        }

        AppUser user = appUserRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new AuthException("존재하지 않는 유저입니다."));

        return jwtProvider.generateAccessToken(user.getId(), user.getRole());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByToken(refreshTokenValue);
    }

    @Transactional
    public void agreeTerms(Long userId, TermsRequest request) {
        if (!request.serviceTermsAgreed() || !request.privacyTermsAgreed()) {
            throw new IllegalArgumentException("필수 약관에 동의해주세요.");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        user.agreeTerms();
    }

    private AuthResponse generateAuthResponse(String email, String nickname,
                                               String provider, String providerId) {
        boolean isNewUser = !appUserRepository.existsByProviderAndProviderId(provider, providerId);

        AppUser user = appUserRepository.findByProviderAndProviderId(provider, providerId)
                .map(entity -> entity.update(nickname))
                .orElse(AppUser.builder()
                        .email(email)
                        .nickname(nickname)
                        .provider(provider)
                        .providerId(providerId)
                        .role("ROLE_USER")
                        .build());
        appUserRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken, isNewUser);
    }

    private String createRefreshToken(Long userId) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays))
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}
