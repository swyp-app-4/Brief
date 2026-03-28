// [레포지토리] RefreshToken JPA 레포지토리. 토큰값/userId 기반 삭제 포함.
package com.brife.user.repository;

import com.brife.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);
}
