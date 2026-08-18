package com.brife.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brife.user.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByProviderAndProviderId(String provider, String providerId);
    List<AppUser> findByDeletedAtIsNotNullAndDeletedAtBefore(LocalDateTime cutoff);

    @Query("""
            SELECT u.id FROM AppUser u
            WHERE u.deletedAt IS NOT NULL
              AND u.deletedAt < :cutoff
              AND u.id > :lastId
            ORDER BY u.id
            """)
    List<Long> findExpiredUserIds(@Param("cutoff") LocalDateTime cutoff,
                                  @Param("lastId") Long lastId,
                                  Pageable pageable);
}
