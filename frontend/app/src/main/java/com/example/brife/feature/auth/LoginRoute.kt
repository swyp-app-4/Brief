package com.example.brife.feature.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                LoginNavigationEvent.NavigateToHome -> onNavigateToHome()
                LoginNavigationEvent.NavigateToTerms -> onNavigateToTerms()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        onKakaoClick = {
            loginWithKakao(context) { accessToken ->
                if (accessToken != null) {
                    viewModel.loginWithKakao(accessToken)
                } else {
                    Log.e("KakaoLogin", "Failed to fetch Kakao accessToken")
                    viewModel.onExternalLoginError("카카오 로그인에 실패했습니다. 다시 시도해주세요.")
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
                        Log.e("GoogleLogin", "Failed to fetch Google idToken")
                        viewModel.onExternalLoginError("Google 로그인에 실패했습니다. 다시 시도해주세요.")
                    }
                }
            }
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
                Log.e("KakaoLogin", "KakaoTalk login failed", error)

                UserApiClient.instance.loginWithKakaoAccount(context) { accountToken, accountError ->
                    if (accountError != null) {
                        Log.e("KakaoLogin", "Kakao account login failed", accountError)
                        onResult(null)
                    } else {
                        Log.d("KakaoLogin", "Kakao account login success")
                        onResult(accountToken?.accessToken)
                    }
                }
            } else {
                Log.d("KakaoLogin", "KakaoTalk login success")
                onResult(token?.accessToken)
            }
        }
    } else {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "Kakao account login failed", error)
                onResult(null)
            } else {
                Log.d("KakaoLogin", "Kakao account login success")
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
            } else {
                viewModel.onExternalLoginError("네이버 로그인에 실패했습니다. 다시 시도해주세요.")
            }
        }

        override fun onFailure(httpStatus: Int, message: String) {
            Log.e("NaverLogin", "fail: $message")
            viewModel.onExternalLoginError("네이버 로그인에 실패했습니다. ($message)")
        }

        override fun onError(errorCode: Int, message: String) {
            Log.e("NaverLogin", "error: $message")
            viewModel.onExternalLoginError("네이버 로그인에 실패했습니다. ($message)")
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
            serverClientId = "657753524375-o8j7tmppjkk8pujad9e8um2fi2h8pk63.apps.googleusercontent.com"
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

                onResult(googleIdTokenCredential.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                onResult(null)
            }
        } else {
            onResult(null)
        }
    } catch (e: Exception) {
        onResult(null)
    }
}
