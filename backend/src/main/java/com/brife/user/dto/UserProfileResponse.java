package com.brife.user.dto;

import com.brife.user.domain.AppUser;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final Long id;
    private final String nickname;
    private final String email;
    private final String profileImageUrl;
    private final String provider;

    public UserProfileResponse(AppUser user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.profileImageUrl = user.getProfileImageUrl();
        this.provider = user.getProvider();
    }
}
