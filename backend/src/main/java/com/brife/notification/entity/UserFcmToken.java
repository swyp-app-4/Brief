package com.brife.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_fcm_token")
@Getter
@NoArgsConstructor
public class UserFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String fcmToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public UserFcmToken(Long userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.createdAt = LocalDateTime.now();
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}