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
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()

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
            loginWithNaver(context, viewModel)
        },
        onGoogleClick = {
            scope.launch {
                loginWithGoogle(context) { idToken ->
                    if (idToken != null) {
                        viewModel.loginWithGoogle(idToken)
                    } else {
                        Log.e("GoogleLogin", "Google idToken 획득 실패")
                    }
                }
            }
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
                        Log.d("KakaoLogin", "카카오계정 로그인 성공: ${accountToken?.accessToken}")
                        onResult(accountToken?.accessToken)
                    }
                }
            } else {
                Log.d("KakaoLogin", "카카오톡 로그인 성공: ${token?.accessToken}")
                onResult(token?.accessToken)
            }
        }
    } else {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "카카오계정 로그인 실패", error)
                onResult(null)
            } else {
                Log.d("KakaoLogin", "카카오계정 로그인 성공: ${token?.accessToken}")
                onResult(token?.accessToken)
            }
        }
    }
}

private fun loginWithNaver(
    context: Context,
    viewModel: LoginViewModel
) {
    val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            val accessToken = NaverIdLoginSDK.getAccessToken()
            Log.d("NaverLogin", "accessToken: $accessToken")

            if (accessToken != null) {
                viewModel.loginWithNaver(accessToken)
            }
        }

        override fun onFailure(httpStatus: Int, message: String) {
            Log.e("NaverLogin", "fail: $message")
        }

        override fun onError(errorCode: Int, message: String) {
            Log.e("NaverLogin", "error: $message")
        }
    }

    NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
}

private suspend fun loginWithGoogle(
    context: Context,
    onResult: (String?) -> Unit
) {
    try {
        val credentialManager = CredentialManager.create(context)

        val googleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = "411738653063-rv0rum9cu9ppsc0it3g37s3nbfdm9pap.apps.googleusercontent.com"
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val idToken = googleIdTokenCredential.idToken
                Log.d("GoogleLogin", "idToken: $idToken")
                onResult(idToken)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleLogin", "Google ID Token 파싱 실패", e)
                onResult(null)
            }
        } else {
            Log.e("GoogleLogin", "지원하지 않는 credential 타입")
            onResult(null)
        }
    } catch (e: Exception) {
        Log.e("GoogleLogin", "구글 로그인 실패", e)
        onResult(null)
    }
}