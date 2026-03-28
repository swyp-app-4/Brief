// [엔티티] 소셜 로그인 유저 (provider/providerId 복합 유니크). 약관 동의, 소프트 삭제(deletedAt) 포함.
package com.brife.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String nickname;

    private String profileImageUrl;

    @Column(nullable = false)
    private String provider; 

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private String role = "ROLE_USER";
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean serviceTermsAgreed = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean privacyTermsAgreed = false;

    private LocalDateTime termsAgreedAt;

    public AppUser update(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public AppUser updateProfileImage(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public void agreeTerms() {
        this.serviceTermsAgreed = true;
        this.privacyTermsAgreed = true;
        this.termsAgreedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

}

