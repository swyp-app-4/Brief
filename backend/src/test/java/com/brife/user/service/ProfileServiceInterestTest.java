package com.brife.user.service;

import com.brife.news.domain.Category;
import com.brife.news.domain.CategoryGroup;
import com.brife.news.repository.CategoryGroupRepository;
import com.brife.news.repository.CategoryRepository;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.brife.user.domain.AppUser;
import com.brife.user.domain.UserInterest;
import com.brife.user.dto.InterestRequest;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceInterestTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private UserInterestRepository userInterestRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryGroupRepository categoryGroupRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserFcmTokenRepository userFcmTokenRepository;
    @Mock private NaverUnlinkClient naverUnlinkClient;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                appUserRepository,
                userInterestRepository,
                categoryRepository,
                categoryGroupRepository,
                refreshTokenRepository,
                userFcmTokenRepository,
                naverUnlinkClient);
    }

    @Test
    void repeatedRequestWithDuplicateIdsDoesNotWriteAgain() {
        AppUser user = mock(AppUser.class);
        Category category = category(33L);
        UserInterest existing = UserInterest.builder().user(user).category(category).build();
        when(appUserRepository.findByIdForUpdate(66L)).thenReturn(Optional.of(user));
        when(categoryRepository.findAllById(java.util.Set.of(33L))).thenReturn(List.of(category));
        when(userInterestRepository.findByUserId(66L)).thenReturn(List.of(existing));

        profileService.resetInterests(66L, new InterestRequest(List.of(33L, 33L), List.of()));

        verify(userInterestRepository, never()).deleteAllInBatch(org.mockito.ArgumentMatchers.anyList());
        verify(userInterestRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void deletesRemovedInterestsAndAddsOnlyMissingInterests() {
        AppUser user = mock(AppUser.class);
        Category retainedCategory = category(33L);
        Category addedCategory = category(34L);
        CategoryGroup removedGroup = group(2L);
        CategoryGroup addedGroup = group(3L);
        UserInterest retained = UserInterest.builder().user(user).category(retainedCategory).build();
        UserInterest removed = UserInterest.builder().user(user).categoryGroup(removedGroup).build();
        when(appUserRepository.findByIdForUpdate(66L)).thenReturn(Optional.of(user));
        when(categoryRepository.findAllById(java.util.Set.of(33L, 34L)))
                .thenReturn(List.of(retainedCategory, addedCategory));
        when(categoryGroupRepository.findAllById(java.util.Set.of(3L))).thenReturn(List.of(addedGroup));
        when(userInterestRepository.findByUserId(66L)).thenReturn(List.of(retained, removed));

        profileService.resetInterests(
                66L,
                new InterestRequest(List.of(33L, 34L, 34L), List.of(3L, 3L)));

        verify(userInterestRepository).deleteAllInBatch(List.of(removed));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserInterest>> additions = ArgumentCaptor.forClass(List.class);
        verify(userInterestRepository).saveAll(additions.capture());
        assertThat(additions.getValue()).hasSize(2);
        assertThat(additions.getValue().stream()
                .filter(interest -> interest.getCategory() != null)
                .map(interest -> interest.getCategory().getId()))
                .containsExactly(34L);
        assertThat(additions.getValue().stream()
                .filter(interest -> interest.getCategoryGroup() != null)
                .map(interest -> interest.getCategoryGroup().getId()))
                .containsExactly(3L);
    }

    @Test
    void postIsAlsoIdempotentWhenInterestsAlreadyExist() {
        AppUser user = mock(AppUser.class);
        Category category = category(33L);
        UserInterest existing = UserInterest.builder().user(user).category(category).build();
        when(appUserRepository.findByIdForUpdate(66L)).thenReturn(Optional.of(user));
        when(categoryRepository.findAllById(java.util.Set.of(33L))).thenReturn(List.of(category));
        when(userInterestRepository.findByUserId(66L)).thenReturn(List.of(existing));

        profileService.saveInterests(66L, new InterestRequest(List.of(33L), List.of()));

        verify(userInterestRepository, never()).deleteAllInBatch(org.mockito.ArgumentMatchers.anyList());
        verify(userInterestRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private Category category(Long id) {
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(id);
        return category;
    }

    private CategoryGroup group(Long id) {
        CategoryGroup group = mock(CategoryGroup.class);
        when(group.getId()).thenReturn(id);
        return group;
    }
}
