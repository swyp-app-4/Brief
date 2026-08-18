package com.brife.user.service;

import com.brife.news.domain.Category;
import com.brife.news.domain.CategoryGroup;
import com.brife.news.repository.CategoryGroupRepository;
import com.brife.news.repository.CategoryRepository;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.brife.user.domain.AppUser;
import com.brife.user.domain.UserInterest;
import com.brife.user.dto.InterestRequest;
import com.brife.user.dto.UserProfileResponse;
import com.brife.user.dto.UserProfileUpdate;
import com.brife.user.dto.WithdrawalRequest;
import com.brife.user.exception.InvalidWithdrawalRequestException;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final UserInterestRepository userInterestRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final NaverUnlinkClient naverUnlinkClient;

    public UserProfileResponse getProfile(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        return new UserProfileResponse(user);
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

        return new UserProfileResponse(appUserRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = "top5News", key = "#userId")
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
    @CacheEvict(value = "top5News", key = "#userId")
    public void resetInterests(Long userId, InterestRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        userInterestRepository.deleteByUserId(userId);

        List<UserInterest> interests = buildInterests(user, request);
        userInterestRepository.saveAll(interests);
    }

    @Transactional
    @CacheEvict(value = "top5News", key = "#userId")
    public void deleteUser(Long userId, WithdrawalRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        String naverRefreshToken = request != null ? request.naverRefreshToken() : null;
        if ("naver".equals(user.getProvider())
                && (naverRefreshToken == null || naverRefreshToken.isBlank())) {
            throw new InvalidWithdrawalRequestException("Naver 회원탈퇴에는 refresh token이 필요합니다.");
        }

        refreshTokenRepository.deleteByUserId(userId);
        userFcmTokenRepository.deleteByUserId(userId);
        user.delete();

        if ("naver".equals(user.getProvider())) {
            try {
                naverUnlinkClient.revokeRefreshToken(naverRefreshToken);
            } catch (Exception e) {
                log.warn("Naver 연동 해제 실패: userId={}, errorType={}",
                        userId, e.getClass().getSimpleName());
            }
        }
    }

    private List<UserInterest> buildInterests(AppUser user, InterestRequest request) {
        List<UserInterest> result = new ArrayList<>();

        if (request.getCategoryIds() != null) {
            request.getCategoryIds().forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("카테고리 없음: " + categoryId));
                result.add(UserInterest.builder().user(user).category(category).build());
            });
        }

        if (request.getGroupIds() != null) {
            request.getGroupIds().forEach(groupId -> {
                CategoryGroup group = categoryGroupRepository.findById(groupId)
                        .orElseThrow(() -> new RuntimeException("대분류 없음: " + groupId));
                result.add(UserInterest.builder().user(user).categoryGroup(group).build());
            });
        }

        return result;
    }
}
