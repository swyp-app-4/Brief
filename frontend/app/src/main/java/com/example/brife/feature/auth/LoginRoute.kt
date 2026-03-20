package com.example.brife.feature.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.AuthRepository
import com.kakao.sdk.user.UserApiClient

@Composable
fun LoginRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val repository = AuthRepository(NetworkModule.authApiService)
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onNavigateToHome()
        }
    }

    LaunchedEffect(uiState.showTermsBottomSheet, uiState.isLoading) {
        if (
            !uiState.showTermsBottomSheet &&
            !uiState.isLoading &&
            uiState.isNewUser &&
            uiState.pendingAccessToken != null
        ) {
            onNavigateToOnboarding()
        }
    }

    LaunchedEffect(uiState.isTermsSuccess) {
        if (uiState.isTermsSuccess) {
            onNavigateToOnboarding()
        }
    }

    LoginScreen(
        uiState = uiState,
        onKakaoClick = {
            loginWithKakao(context) { accessToken ->
                if (accessToken != null) {
                    viewModel.loginWithKakao(accessToken)
                } else {
                    Log.e("KakaoLogin", "카카오 accessToken 획득 실패")
                }
            }
        },
        onNaverClick = {
            viewModel.loginWithNaver("naver_sdk_access_token")
        },
        onGoogleClick = {
            viewModel.loginWithGoogle("google_sdk_id_token")
        },
        onDismissTerms = {
            viewModel.dismissTermsBottomSheet()
        },
        onAgreeTerms = {
            viewModel.agreeTerms()
        }
    )
}

private fun loginWithKakao(
    context: Context,
    onResult: (String?) -> Unit
) {
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "카카오톡 로그인 실패", error)

                UserApiClient.instance.loginWithKakaoAccount(context) { accountToken, accountError ->
                    if (accountError != null) {
                        Log.e("KakaoLogin", "카카오계정 로그인 실패", accountError)
                        onResult(null)
                    } else {
                        onResult(accountToken?.accessToken)
                    }
                }
            } else {
                onResult(token?.accessToken)
            }
        }
    } else {
//        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
//            if (error != null) {
//                Log.e("KakaoLogin", "카카오계정 로그인 실패", error)
//                onResult(null)
//            } else {
//                onResult(token?.accessToken)
//            }
//        }

        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "카카오계정 로그인 실패", error)
            } else if (token != null) {
                Log.d("KakaoLogin", "로그인 성공: ${token.accessToken}")
            }
        }
    }
}