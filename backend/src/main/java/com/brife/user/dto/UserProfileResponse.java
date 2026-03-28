// [DTO - 응답] 유저 프로필 응답 (id, nickname, email, profileImageUrl, provider, interests, groupInterests).
package com.brife.user.dto;

import com.brife.category.dto.CategoryGroupResponse;
import com.brife.category.dto.CategoryResponse;
import com.brife.user.domain.AppUser;
import com.brife.user.domain.UserInterest;
import lombok.Getter;

import java.util.List;

@Getter
public class UserProfileResponse {

    private final Long id;
    private final String nickname;
    private final String email;
    private final String profileImageUrl;
    private final String provider;
    private final List<CategoryResponse> interests;
    private final List<CategoryGroupResponse> groupInterests;

    public UserProfileResponse(AppUser user, List<UserInterest> interests) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.profileImageUrl = user.getProfileImageUrl();
        this.provider = user.getProvider();
        this.interests = interests.stream()
                .filter(ui -> ui.getCategory() != null)
                .map(ui -> new CategoryResponse(ui.getCategory()))
                .toList();
        this.groupInterests = interests.stream()
                .filter(ui -> ui.getCategoryGroup() != null)
                .map(ui -> new CategoryGroupResponse(ui.getCategoryGroup()))
                .toList();
    }
}
