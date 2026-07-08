package com.brife.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_notification_setting")
@Getter
@NoArgsConstructor
public class UserNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean dailyNewsEnabled = false;

    @Column(nullable = false)
    private boolean time8am = false;

    @Column(nullable = false)
    private boolean time12pm = false;

    @Column(nullable = false)
    private boolean time6pm = false;

    @Column(nullable = false)
    private boolean time10pm = false;

    public UserNotificationSetting(Long userId) {
        this.userId = userId;
    }

    public void update(boolean dailyNewsEnabled, boolean time8am, boolean time12pm, boolean time6pm, boolean time10pm) {
        this.dailyNewsEnabled = dailyNewsEnabled;
        this.time8am = time8am;
        this.time12pm = time12pm;
        this.time6pm = time6pm;
        this.time10pm = time10pm;
    }
}