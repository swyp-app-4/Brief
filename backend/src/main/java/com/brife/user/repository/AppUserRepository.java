// [레포지토리] AppUser JPA 레포지토리. provider 기반 조회, 탈퇴 대상 유저 조회 포함.
package com.brife.user.repository;

import com.brife.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByProviderAndProviderId(String provider, String providerId);
    List<AppUser> findByDeletedAtIsNotNullAndDeletedAtBefore(LocalDateTime cutoff);
}
