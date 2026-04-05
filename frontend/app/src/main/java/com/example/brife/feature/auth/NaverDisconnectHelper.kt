package com.example.brife.feature.auth

import android.util.Log
import com.navercorp.nid.NaverIdLoginSDK

/**
 * 네이버 SDK의 NaverIdLoginSDK.logout()을 Result로 감싼 헬퍼.
 *
 * NaverIdLoginSDK.logout():
 *   - 로컬에 저장된 Access Token / Refresh Token 즉시 삭제 (동기)
 *   - 서버 측 토큰 폐기는 백엔드 DELETE /users/me 에서 처리
 *
 * 카카오 unlink, 구글 clearCredentialState와 동일한 Result 패턴.
 * 성공 시 Result.success(Unit), 실패 시 Result.failure(exception) 반환.
 */
fun naverDisconnect(): Result<Unit> = runCatching {
    NaverIdLoginSDK.logout()
    Log.i("NaverDisconnect", "네이버 연동 해제(로컬 토큰 삭제) 성공")
}
