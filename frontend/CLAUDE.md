# Brife Android Project - Claude Instructions

## Project Goal
- This is an Android app built with Jetpack Compose.
- Current focus: UI screen development (no API integration yet).
- API integration will be done later.

## Git Restrictions (VERY IMPORTANT)
- NEVER perform git actions
- Do NOT run git commit, reset, push, or checkout

## Code Modification Rules
- You ARE allowed to modify code files
- You ARE allowed to create new files if necessary

BUT:
- Do NOT refactor the entire project
- Do NOT modify multiple unrelated files at once
- Always explain what you are changing and why
- Keep changes minimal and safe

## Architecture
This project follows MVVM with the following structure:

- Screen.kt → Pure UI (no business logic)
- Route.kt → Connects ViewModel and handles navigation
- ViewModel.kt → StateFlow-based state management
- Repository → Currently returns mock data (API will be connected later)

## Critical Rules
1. Screen must NOT know where data comes from
2. Screen must NOT directly use mock data
3. All data must come from ViewModel via parameters
4. Keep UI and logic strictly separated

Example:

Correct:
```kotlin
@Composable
fun HomeScreen(
    newsList: List<HomeNewsItem>
)
```

Wrong:
```kotlin
val list = ShortSampleData.items
```

## Current Development Phase (IMPORTANT)
- API is NOT connected yet
- UI must be built using mock data
- Focus on:
  - Screen layout
  - Navigation flow
  - UI consistency
  
## Today's Priority
1. MainScreen (bottom navigation structure)
2. HomeScreen (news card UI using mock data)
3. NewsLongScreen (detail UI)
4. Onboarding screens
5. Login screens
6. Archive screens

## Mock Data Usage

Allowed:
- ShortSampleData.kt
- FolderUiModel.kt
BUT:
- Use mock data ONLY inside ViewModel
- Do NOT use mock data directly in Screen

## UI Component Strategy

Reuse existing components:

- AppTopBar
- AppNavigationBar
- PrimaryButton
- CategoryChip
- InterestCard
- AppText

New components to stabilize early:

- HomeNewsCard
- Bookmark BottomSheet
- Folder Grid Item

## Task Guidelines
When suggesting implementation:

- Focus on ONE screen at a time
- Do NOT suggest full project refactoring
- Do NOT modify multiple unrelated files at once
- Suggest minimal, safe changes only

## Response Style
- Explain clearly before modifying code
- Prefer Korean explanations
- If uncertain, say "불확실"
- Do NOT guess missing code