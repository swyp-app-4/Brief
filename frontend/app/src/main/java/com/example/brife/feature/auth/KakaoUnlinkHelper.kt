package com.example.brife.feature.auth

import android.util.Log
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 카카오 SDK의 콜백 기반 unlink()를 suspend 함수로 감싼 헬퍼.
 * 성공 시 Result.success(Unit), 실패 시 Result.failure(exception) 반환.
 */
suspend fun kakaoUnlink(): Result<Unit> = try {
    suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e("KakaoUnlink", "카카오 연결 해제 실패", error)
                if (continuation.isActive) continuation.resumeWithException(error)
            } else {
                Log.i("KakaoUnlink", "카카오 연결 해제 성공")
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }
    Result.success(Unit)
} catch (e: Exception) {
    Log.e("KakaoUnlink", "kakaoUnlink 예외: ${e.message}")
    Result.failure(e)
}
