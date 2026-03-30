package com.brife.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdate {

    private String nickname;
    private String profileImageUrl;
}
