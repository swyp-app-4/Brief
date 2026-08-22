package com.brife.notification.service;

import com.brife.notification.dto.response.NotificationSettingResponse;
import com.brife.notification.entity.UserFcmToken;
import com.brife.notification.entity.UserNotificationSetting;
import com.brife.notification.repository.UserFcmTokenRepository;
import com.brife.notification.repository.UserNotificationSettingRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;
    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;
    @Mock
    private TopNewsNotificationPayloadFactory topNewsNotificationPayloadFactory;
    @Mock
    private FirebaseMessageSender firebaseMessageSender;
    @InjectMocks
    private NotificationSettingService service;

    @Test
    void returnsDisabledDefaultsWithoutPersistingWhenSettingsDoNotExist() {
        when(userNotificationSettingRepository.findByUserId(7L)).thenReturn(Optional.empty());

        NotificationSettingResponse response = service.getSettings(7L);

        assertDisabled(response);
        verify(userNotificationSettingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnsExistingSettingsWithoutChangingThem() {
        UserNotificationSetting setting = new UserNotificationSetting(7L);
        setting.update(true, true, false, true, false);
        when(userNotificationSettingRepository.findByUserId(7L)).thenReturn(Optional.of(setting));

        NotificationSettingResponse response = service.getSettings(7L);

        assertThat(response.isDailyNewsEnabled()).isTrue();
        assertThat(response.isTime8am()).isTrue();
        assertThat(response.isTime12pm()).isFalse();
        assertThat(response.isTime6pm()).isTrue();
        assertThat(response.isTime10pm()).isFalse();
        verify(userNotificationSettingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void concurrentReadsOfMissingSettingsDoNotAttemptToInsert() throws Exception {
        when(userNotificationSettingRepository.findByUserId(7L)).thenReturn(Optional.empty());
        List<Callable<NotificationSettingResponse>> requests = java.util.stream.IntStream.range(0, 16)
                .mapToObj(ignored -> (Callable<NotificationSettingResponse>) () -> service.getSettings(7L))
                .toList();

        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            List<NotificationSettingResponse> responses = executor.invokeAll(requests).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new AssertionError(e);
                        }
                    })
                    .toList();

            assertThat(responses).hasSize(16).allSatisfy(this::assertDisabled);
        }
        verify(userNotificationSettingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void removesOnlyTheTokenThatFirebaseReportsAsUnregistered() throws Exception {
        UserFcmToken token = new UserFcmToken(7L, "expired-token");
        FirebaseMessagingException exception = firebaseException(MessagingErrorCode.UNREGISTERED);
        when(userFcmTokenRepository.findByUserId(7L)).thenReturn(Optional.of(token));
        when(topNewsNotificationPayloadFactory.create(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Map.of());
        when(firebaseMessageSender.send(org.mockito.ArgumentMatchers.any(Message.class))).thenThrow(exception);
        when(userFcmTokenRepository.deleteByUserIdAndFcmToken(7L, "expired-token")).thenReturn(1);

        PushDeliveryResult result = service.sendTop5NewsNotification(
                7L, com.brife.notification.NewsNotificationSlot.MORNING, List.of());

        assertThat(result).isEqualTo(PushDeliveryResult.INVALID_TOKEN_REMOVED);
        verify(userFcmTokenRepository).deleteByUserIdAndFcmToken(7L, "expired-token");
        verify(userFcmTokenRepository, never()).deleteByUserId(7L);
    }

    @ParameterizedTest
    @EnumSource(value = MessagingErrorCode.class, names = {
            "THIRD_PARTY_AUTH_ERROR", "INTERNAL", "QUOTA_EXCEEDED", "UNAVAILABLE"
    })
    void keepsTokenForTransientOrNonTokenFirebaseErrors(MessagingErrorCode errorCode) throws Exception {
        UserFcmToken token = new UserFcmToken(7L, "active-token");
        FirebaseMessagingException exception = firebaseException(errorCode);
        when(userFcmTokenRepository.findByUserId(7L)).thenReturn(Optional.of(token));
        when(topNewsNotificationPayloadFactory.create(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Map.of());
        when(firebaseMessageSender.send(org.mockito.ArgumentMatchers.any(Message.class))).thenThrow(exception);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.sendTop5NewsNotification(
                        7L, com.brife.notification.NewsNotificationSlot.MORNING, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        verify(userFcmTokenRepository, never()).deleteByUserIdAndFcmToken(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void doesNotRepeatFirebaseCallAfterInvalidTokenWasRemoved() throws Exception {
        UserFcmToken token = new UserFcmToken(7L, "expired-token");
        FirebaseMessagingException exception = firebaseException(MessagingErrorCode.UNREGISTERED);
        when(userFcmTokenRepository.findByUserId(7L))
                .thenReturn(Optional.of(token), Optional.empty());
        when(topNewsNotificationPayloadFactory.create(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Map.of());
        when(firebaseMessageSender.send(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenThrow(exception);

        PushDeliveryResult first = service.sendTop5NewsNotification(
                7L, com.brife.notification.NewsNotificationSlot.MORNING, List.of());
        PushDeliveryResult second = service.sendTop5NewsNotification(
                7L, com.brife.notification.NewsNotificationSlot.LUNCH, List.of());

        assertThat(first).isEqualTo(PushDeliveryResult.INVALID_TOKEN_REMOVED);
        assertThat(second).isEqualTo(PushDeliveryResult.SKIPPED_NO_TOKEN);
        verify(firebaseMessageSender).send(org.mockito.ArgumentMatchers.any(Message.class));
    }

    @Test
    void conditionalDeleteDoesNotRemoveAReplacementToken() throws Exception {
        UserFcmToken token = new UserFcmToken(7L, "expired-token");
        FirebaseMessagingException exception = firebaseException(MessagingErrorCode.UNREGISTERED);
        when(userFcmTokenRepository.findByUserId(7L)).thenReturn(Optional.of(token));
        when(topNewsNotificationPayloadFactory.create(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Map.of());
        when(firebaseMessageSender.send(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenThrow(exception);
        when(userFcmTokenRepository.deleteByUserIdAndFcmToken(7L, "expired-token")).thenReturn(0);

        PushDeliveryResult result = service.sendTop5NewsNotification(
                7L, com.brife.notification.NewsNotificationSlot.MORNING, List.of());

        assertThat(result).isEqualTo(PushDeliveryResult.INVALID_TOKEN_REMOVED);
        verify(userFcmTokenRepository).deleteByUserIdAndFcmToken(7L, "expired-token");
    }

    private FirebaseMessagingException firebaseException(MessagingErrorCode errorCode) {
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(errorCode);
        return exception;
    }

    private void assertDisabled(NotificationSettingResponse response) {
        assertThat(response.isDailyNewsEnabled()).isFalse();
        assertThat(response.isTime8am()).isFalse();
        assertThat(response.isTime12pm()).isFalse();
        assertThat(response.isTime6pm()).isFalse();
        assertThat(response.isTime10pm()).isFalse();
    }
}
