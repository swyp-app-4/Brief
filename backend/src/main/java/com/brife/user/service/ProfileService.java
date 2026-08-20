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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        replaceInterests(userId, request);
    }

    @Transactional
    @CacheEvict(value = "top5News", key = "#userId")
    public void resetInterests(Long userId, InterestRequest request) {
        replaceInterests(userId, request);
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

    private void replaceInterests(Long userId, InterestRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("관심사 요청이 필요합니다.");
        }

        AppUser user = appUserRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        Set<Long> requestedCategoryIds = normalizeIds(request.getCategoryIds(), "카테고리");
        Set<Long> requestedGroupIds = normalizeIds(request.getGroupIds(), "대분류");
        Map<Long, Category> categories = loadCategories(requestedCategoryIds);
        Map<Long, CategoryGroup> groups = loadCategoryGroups(requestedGroupIds);

        List<UserInterest> existingInterests = userInterestRepository.findByUserId(userId);
        Set<Long> retainedCategoryIds = new LinkedHashSet<>();
        Set<Long> retainedGroupIds = new LinkedHashSet<>();
        List<UserInterest> interestsToDelete = existingInterests.stream()
                .filter(interest -> shouldDelete(
                        interest,
                        requestedCategoryIds,
                        requestedGroupIds,
                        retainedCategoryIds,
                        retainedGroupIds))
                .toList();

        if (!interestsToDelete.isEmpty()) {
            userInterestRepository.deleteAllInBatch(interestsToDelete);
        }

        List<UserInterest> interestsToAdd = buildMissingInterests(
                user,
                requestedCategoryIds,
                requestedGroupIds,
                retainedCategoryIds,
                retainedGroupIds,
                categories,
                groups);
        if (!interestsToAdd.isEmpty()) {
            userInterestRepository.saveAll(interestsToAdd);
        }
    }

    private Set<Long> normalizeIds(List<Long> ids, String type) {
        if (ids == null) {
            return Set.of();
        }
        if (ids.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException(type + " ID에 null을 포함할 수 없습니다.");
        }
        return new LinkedHashSet<>(ids);
    }

    private Map<Long, Category> loadCategories(Set<Long> requestedIds) {
        Map<Long, Category> categories = categoryRepository.findAllById(requestedIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        validateIds(requestedIds, categories, "카테고리");
        return categories;
    }

    private Map<Long, CategoryGroup> loadCategoryGroups(Set<Long> requestedIds) {
        Map<Long, CategoryGroup> groups = categoryGroupRepository.findAllById(requestedIds).stream()
                .collect(Collectors.toMap(CategoryGroup::getId, Function.identity()));
        validateIds(requestedIds, groups, "대분류");
        return groups;
    }

    private void validateIds(Set<Long> requestedIds, Map<Long, ?> entities, String type) {
        if (entities.size() == requestedIds.size()) {
            return;
        }
        Set<Long> missingIds = new LinkedHashSet<>(requestedIds);
        missingIds.removeAll(entities.keySet());
        throw new IllegalArgumentException("존재하지 않는 " + type + " ID: " + missingIds);
    }

    private boolean shouldDelete(UserInterest interest,
                                 Set<Long> requestedCategoryIds,
                                 Set<Long> requestedGroupIds,
                                 Set<Long> retainedCategoryIds,
                                 Set<Long> retainedGroupIds) {
        if (interest.getCategory() != null) {
            Long categoryId = interest.getCategory().getId();
            return !requestedCategoryIds.contains(categoryId) || !retainedCategoryIds.add(categoryId);
        }
        if (interest.getCategoryGroup() != null) {
            Long groupId = interest.getCategoryGroup().getId();
            return !requestedGroupIds.contains(groupId) || !retainedGroupIds.add(groupId);
        }
        return true;
    }

    private List<UserInterest> buildMissingInterests(AppUser user,
                                                     Set<Long> requestedCategoryIds,
                                                     Set<Long> requestedGroupIds,
                                                     Set<Long> retainedCategoryIds,
                                                     Set<Long> retainedGroupIds,
                                                     Map<Long, Category> categories,
                                                     Map<Long, CategoryGroup> groups) {
        List<UserInterest> interests = new ArrayList<>();
        requestedCategoryIds.stream()
                .filter(id -> !retainedCategoryIds.contains(id))
                .forEach(id -> interests.add(
                        UserInterest.builder().user(user).category(categories.get(id)).build()));
        requestedGroupIds.stream()
                .filter(id -> !retainedGroupIds.contains(id))
                .forEach(id -> interests.add(
                        UserInterest.builder().user(user).categoryGroup(groups.get(id)).build()));
        return List.copyOf(interests);
    }
}
