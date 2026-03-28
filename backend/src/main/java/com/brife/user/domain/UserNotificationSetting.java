// [엔티티] 유저 알림 설정 (전체/오전/점심/저녁/자기전). 기본값: 오전·점심 on, 저녁·자기전 off.
package com.brife.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false)
    @Builder.Default
    private boolean notificationEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean morningEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean lunchEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean eveningEnabled = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean nightEnabled = false;

    public void update(boolean notificationEnabled, boolean morningEnabled,
                       boolean lunchEnabled, boolean eveningEnabled, boolean nightEnabled) {
        this.notificationEnabled = notificationEnabled;
        this.morningEnabled = morningEnabled;
        this.lunchEnabled = lunchEnabled;
        this.eveningEnabled = eveningEnabled;
        this.nightEnabled = nightEnabled;
    }
}
