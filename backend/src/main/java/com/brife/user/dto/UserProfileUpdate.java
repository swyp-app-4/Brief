// [DTO - 요청] 프로필 수정 요청 (nickname, profileImageUrl). null이면 해당 필드 미변경.
package com.brife.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdate {

    private String nickname;
    private String profileImageUrl;
}
