package com.brife.user.social;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brife.user.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> 
{
    Optional<AppUser> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByProviderAndProviderId(String provider, String providerId);
}