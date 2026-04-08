package com.swyp.brife.feature.auth

import android.util.Log
import com.navercorp.nid.NaverIdLoginSDK

/**
 * 회원탈퇴 시 네이버 SDK 로컬 토큰 삭제.
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

/**
 * 로그아웃 시 네이버 SDK 로컬 세션 정리.
 *
 * NaverIdLoginSDK.logout()으로 로컬 캐시 토큰만 삭제.
 * → 다음 로그인 시 계정 선택 화면이 다시 표시됨.
 * → 서버 측 계정 연동 해제(탈퇴)와는 무관.
 */
fun naverLogout(): Result<Unit> = runCatching {
    NaverIdLoginSDK.logout()
    Log.i("NaverLogout", "네이버 SDK 로컬 세션 정리 완료")
}
