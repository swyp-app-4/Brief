package com.brife.user.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    private static final String SECRET = "test-secret-key-must-be-at-least-32-characters";
    private static final long ACCESS_EXPIRATION_MS = 3600000L; // 1시간

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_EXPIRATION_MS);
    }

    @Test
    @DisplayName("토큰 생성 후 userId와 role을 올바르게 파싱한다")
    void generateAndParseToken() {
        String token = jwtProvider.generateAccessToken(1L, "ROLE_USER");

        Claims claims = jwtProvider.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("유효한 토큰은 isValid가 true를 반환한다")
    void validToken() {
        String token = jwtProvider.generateAccessToken(1L, "ROLE_USER");

        assertThat(jwtProvider.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰은 isValid가 false를 반환한다")
    void tamperedToken() {
        String token = jwtProvider.generateAccessToken(1L, "ROLE_USER");
        String tampered = token + "tampered";

        assertThat(jwtProvider.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 isValid가 false를 반환한다")
    void expiredToken() {
        JwtProvider shortLivedProvider = new JwtProvider(SECRET, -1L);
        String token = shortLivedProvider.generateAccessToken(1L, "ROLE_USER");

        assertThat(jwtProvider.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("빈 문자열은 isValid가 false를 반환한다")
    void emptyToken() {
        assertThat(jwtProvider.isValid("")).isFalse();
    }

    @Test
    @DisplayName("서로 다른 userId로 생성한 토큰은 subject가 다르다")
    void differentUserIds() {
        String token1 = jwtProvider.generateAccessToken(1L, "ROLE_USER");
        String token2 = jwtProvider.generateAccessToken(2L, "ROLE_USER");

        assertThat(jwtProvider.parseToken(token1).getSubject())
                .isNotEqualTo(jwtProvider.parseToken(token2).getSubject());
    }
}
