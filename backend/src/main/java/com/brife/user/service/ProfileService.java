// [프로필 서비스] 프로필 조회/수정, 관심사 저장(POST)/재설정(PUT), 회원 탈퇴(소프트 삭제).
package com.brife.user.service;

import com.brife.category.domain.Category;
import com.brife.category.domain.CategoryGroup;
import com.brife.category.repository.CategoryGroupRepository;
import com.brife.category.repository.CategoryRepository;
import com.brife.user.domain.AppUser;
import com.brife.user.domain.UserInterest;
import com.brife.user.dto.InterestRequest;
import com.brife.user.dto.UserProfileResponse;
import com.brife.user.dto.UserProfileUpdate;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final UserInterestRepository userInterestRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserProfileResponse getProfile(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return new UserProfileResponse(user, userInterestRepository.findByUserId(userId));
    }

    public UserProfileResponse updateProfile(Long userId, UserProfileUpdate request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (request.getNickname() != null) {
            user.update(request.getNickname());
        }

        if (request.getProfileImageUrl() != null) {
            user.updateProfileImage(request.getProfileImageUrl());
        }

        AppUser saved = appUserRepository.save(user);
        return new UserProfileResponse(saved, userInterestRepository.findByUserId(userId));
    }

    @Transactional
    public void saveInterests(Long userId, InterestRequest request) {
        if (userInterestRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 관심사가 설정되어 있습니다. 재설정은 PUT을 사용하세요.");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        List<UserInterest> interests = buildInterests(user, request);
        userInterestRepository.saveAll(interests);
    }

    @Transactional
    public void resetInterests(Long userId, InterestRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        userInterestRepository.deleteByUserId(userId);

        List<UserInterest> interests = buildInterests(user, request);
        userInterestRepository.saveAll(interests);
    }

    @Transactional
    public void deleteUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        refreshTokenRepository.deleteByUserId(userId);
        user.delete();
        appUserRepository.save(user);
    }

    private List<UserInterest> buildInterests(AppUser user, InterestRequest request) {
        List<Long> categoryIds = request.getCategoryIds() != null ? request.getCategoryIds() : List.of();
        List<Long> groupIds = request.getGroupIds() != null ? request.getGroupIds() : List.of();

        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            throw new IllegalArgumentException("관심사를 하나 이상 선택해주세요.");
        }

        List<UserInterest> interests = new ArrayList<>();

        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                throw new RuntimeException("존재하지 않는 소분류 카테고리가 포함되어 있습니다.");
            }
            categories.forEach(category -> interests.add(
                    UserInterest.builder().user(user).category(category).build()
            ));
        }

        if (!groupIds.isEmpty()) {
            List<CategoryGroup> groups = categoryGroupRepository.findAllById(groupIds);
            if (groups.size() != groupIds.size()) {
                throw new RuntimeException("존재하지 않는 대분류 카테고리가 포함되어 있습니다.");
            }
            groups.forEach(group -> interests.add(
                    UserInterest.builder().user(user).categoryGroup(group).build()
            ));
        }

        return interests;
    }
}
