package com.example.brife.feature.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException

/**
 * Google Credential Manager의 clearCredentialState()를 Result로 감싼 헬퍼.
 * 앱 내 credential 상태를 초기화하여 다음 로그인 시 계정 선택 UI가 다시 표시되게 한다.
 * (Google 계정 revoke는 이 함수의 범위에 포함되지 않음)
 *
 * 성공 시 Result.success(Unit), 실패 시 Result.failure(exception) 반환.
 */
suspend fun googleClearCredentialState(context: Context): Result<Unit> = try {
    val credentialManager = CredentialManager.create(context)
    credentialManager.clearCredentialState(ClearCredentialStateRequest())
    Log.i("GoogleClear", "clearCredentialState 성공")
    Result.success(Unit)
} catch (e: ClearCredentialException) {
    Log.e("GoogleClear", "clearCredentialState 실패: ${e.message}", e)
    Result.failure(e)
} catch (e: Exception) {
    Log.e("GoogleClear", "clearCredentialState 예외: ${e.message}", e)
    Result.failure(e)
}
