package com.example.brife.navigation

object NavRoutes {

    const val AUTH = "auth"
    const val SPLASH = "splash"

    const val ONBOARDING_GUIDE = "onboarding_guide"
    const val ONBOARDING_INTEREST = "onboarding_interest"

    const val ONBOARDING_SUB_INTEREST = "onboarding_sub_interest"

    const val LOGIN = "login"
    const val LOGIN_TERMS = "login_terms"

    // 메인 영역 (하단 바가 있는 영역)
    const val MAIN = "main"

    // 하단 바 탭들 (MainScreen 내부에서 사용)
    const val HOME = "home"
    const val NEWS_LONG = "news_long"
    const val EXPLORE = "explore"
    const val ARCHIVE = "archive"
    const val ARCHIVE_DETAIL = "archive_detail"   // 추가: 폴더 상세 화면 (폴더 이름을 인자로 받을 수 있도록 설정 가능)
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile_edit"

    // 비로그인 프로필에서 관심사 재설정 플로우 (MainScreen 내부 NavHost 전용)
    const val ONBOARDING_INTEREST_RESET = "onboarding_interest_reset"
    const val ONBOARDING_SUB_INTEREST_RESET = "onboarding_sub_interest_reset"

    // 설정 화면 (MainScreen 바깥 독립 라우트)
    const val SETTING = "setting"
    const val WIDGET_INSTALL_GUIDE = "widget_install_guide"
    const val WEB_VIEW = "web_view"
    const val INQUIRY = "inquiry"
}
