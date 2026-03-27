package com.example.brife.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private enum class LoginTermsPage {
    MAIN, SERVICE_DETAIL, PRIVACY_DETAIL
}

@Composable
fun LoginTermsRoute(
    viewModel: LoginViewModel,
    onNavigateToOnboarding: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 각 약관의 확정된 동의 상태 (상세 화면에서 "다음" 클릭 시에만 갱신)
    var serviceConfirmed by remember { mutableStateOf(false) }
    var privacyConfirmed by remember { mutableStateOf(false) }

    // 상세 화면 내 임시 체크 상태 (취소 시 confirmed에 반영되지 않음)
    var serviceChecked by remember { mutableStateOf(false) }
    var privacyChecked by remember { mutableStateOf(false) }

    // 현재 표시할 화면
    var currentPage by remember { mutableStateOf(LoginTermsPage.MAIN) }

    // 전체동의 순차 플로우 여부 (Service → Privacy 자동 이동)
    var isAllAgreeFlow by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isTermsSuccess) {
        if (uiState.isTermsSuccess) {
            onNavigateToOnboarding()
        }
    }

    when (currentPage) {
        LoginTermsPage.SERVICE_DETAIL -> {
            ServiceTermsDetailScreen(
                isChecked = serviceChecked,
                onCheckChange = { serviceChecked = it },
                onNextClick = {
                    serviceConfirmed = true
                    if (isAllAgreeFlow) {
                        // 전체동의 플로우: 서비스 약관 완료 → 개인정보 처리 동의서로 이동
                        privacyChecked = privacyConfirmed
                        currentPage = LoginTermsPage.PRIVACY_DETAIL
                    } else {
                        currentPage = LoginTermsPage.MAIN
                    }
                },
                onBackClick = {
                    // 체크하지 않고 뒤로가면 serviceConfirmed 변경 없음
                    isAllAgreeFlow = false
                    currentPage = LoginTermsPage.MAIN
                }
            )
        }

        LoginTermsPage.PRIVACY_DETAIL -> {
            PrivacyTermsDetailScreen(
                isChecked = privacyChecked,
                onCheckChange = { privacyChecked = it },
                onNextClick = {
                    privacyConfirmed = true
                    isAllAgreeFlow = false
                    currentPage = LoginTermsPage.MAIN
                },
                onBackClick = {
                    // 체크하지 않고 뒤로가면 privacyConfirmed 변경 없음
                    isAllAgreeFlow = false
                    currentPage = LoginTermsPage.MAIN
                }
            )
        }

        LoginTermsPage.MAIN -> {
            LoginTermsScreen(
                isLoading = uiState.isLoading,
                onNext = { viewModel.agreeTerms() },
                onBack = onBackClick,
                onServiceDetailClick = {
                    serviceChecked = serviceConfirmed
                    currentPage = LoginTermsPage.SERVICE_DETAIL
                },
                onPrivacyDetailClick = {
                    privacyChecked = privacyConfirmed
                    currentPage = LoginTermsPage.PRIVACY_DETAIL
                },
                onAllAgreeClick = {
                    // 전체동의 플로우: ServiceTermsDetail → PrivacyTermsDetail → MAIN 순서로 이동
                    isAllAgreeFlow = true
                    serviceChecked = serviceConfirmed
                    currentPage = LoginTermsPage.SERVICE_DETAIL
                },
                initialServiceAgree = serviceConfirmed,
                initialPrivacyAgree = privacyConfirmed
            )
        }
    }
}
