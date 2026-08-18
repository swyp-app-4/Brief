package com.brife.user.scheduler;

import com.brife.user.repository.AppUserRepository;
import com.brife.user.service.ExpiredUserDeletionService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCleanupSchedulerTest {

    @Test
    void runsOnceADayAtMidnightInSeoul() throws NoSuchMethodException {
        Scheduled scheduled = UserCleanupScheduler.class
                .getDeclaredMethod("hardDeleteExpiredUsers")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled.cron()).isEqualTo("0 0 0 * * *");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }

    @Test
    void continuesWithNextUserWhenOneDeletionFails() {
        AppUserRepository users = mock(AppUserRepository.class);
        ExpiredUserDeletionService deletionService = mock(ExpiredUserDeletionService.class);
        when(users.findExpiredUserIds(any(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(1L, 2L), List.of());
        when(deletionService.deleteByUserId(1L)).thenThrow(new RuntimeException("delete failed"));
        when(deletionService.deleteByUserId(2L)).thenReturn(true);

        new UserCleanupScheduler(users, deletionService).hardDeleteExpiredUsers();

        verify(deletionService).deleteByUserId(1L);
        verify(deletionService).deleteByUserId(2L);
    }
}
