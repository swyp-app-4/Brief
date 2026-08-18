package com.brife.user.service;

import com.brife.archive.repository.ArchiveItemRepository;
import com.brife.archive.repository.ArchiveRepository;
import com.brife.user.repository.AppUserRepository;
import com.brife.user.repository.RefreshTokenRepository;
import com.brife.user.repository.UserInterestRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpiredUserDeletionServiceTest {

    @Test
    void deletesAllRelatedDataBeforeUser() {
        AppUserRepository users = mock(AppUserRepository.class);
        UserInterestRepository interests = mock(UserInterestRepository.class);
        RefreshTokenRepository tokens = mock(RefreshTokenRepository.class);
        ArchiveItemRepository items = mock(ArchiveItemRepository.class);
        ArchiveRepository archives = mock(ArchiveRepository.class);
        when(users.existsById(1L)).thenReturn(true);
        ExpiredUserDeletionService service = new ExpiredUserDeletionService(
                users, interests, tokens, items, archives);

        assertThat(service.deleteByUserId(1L)).isTrue();

        InOrder order = inOrder(items, archives, interests, tokens, users);
        order.verify(items).deleteByUserId(1L);
        order.verify(archives).deleteByUserId(1L);
        order.verify(interests).deleteByUserId(1L);
        order.verify(tokens).deleteByUserId(1L);
        order.verify(users).deleteById(1L);
    }
}
