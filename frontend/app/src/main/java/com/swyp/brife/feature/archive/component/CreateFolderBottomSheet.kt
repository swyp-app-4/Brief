package com.swyp.brife.feature.archive.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.brife.ui.theme.brifeColors
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.DarkBackground
import com.swyp.brife.ui.theme.DarkFolderHelperText
import com.swyp.brife.ui.theme.DarkGray300
import com.swyp.brife.ui.theme.DarkGray600
import com.swyp.brife.ui.theme.DarkTextTitle
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.PrimaryButtonTextStyle
import com.swyp.brife.ui.theme.Pretendard
import com.swyp.brife.ui.theme.Negative
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextTitle
import com.swyp.brife.ui.theme.TextBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
    currentFolderCount: Int = 1,
    existingFolders: List<String> = listOf("즐겨찾기"),
    title: String = "새 폴더 만들기",       // 수정 바텀시트 재사용 시 "폴더명 수정"으로 전달
    initialFolderName: String = ""          // 수정 시 기존 폴더명을 초기값으로 전달
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val sheetState = rememberModalBottomSheetState()
    var folderName by remember { mutableStateOf(initialFolderName) }
    var isDirty by remember { mutableStateOf(false) }

    // --- 검증 로직 ---
    val hasSpecialChars = folderName.any { it == '/' || it == ';' || it == ':' }
    val isUsedName = existingFolders.contains(folderName.trim())
    val isLimitExceeded = currentFolderCount >= 20
    val isEmptyAfterInput = isDirty && folderName.isBlank()

    // 에러 상태 판단
    val isError = hasSpecialChars || isUsedName || isLimitExceeded || isEmptyAfterInput

    // 도우미 텍스트 결정
    val helperText = when {
        isLimitExceeded -> "폴더는 최대 20개까지만 만들 수 있어요."
        hasSpecialChars -> "특수문자(/,;,:)는 폴더 이름에 쓸 수 없어요."
        isUsedName -> "이미 사용중인 폴더 이름이에요."
        isEmptyAfterInput -> "폴더 이름을 입력해주세요."
        else -> "최대 20자"
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (isDarkTheme) DarkGray300 else MaterialTheme.brifeColors.backgroundDefault,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(top = 32.dp, bottom = 40.dp)
        ) {
            // 1. 타이틀
            AppText(
                text = title,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 26.4.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = if (isDarkTheme) DarkTextTitle else TextTitle
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2. 라벨
            AppText(
                text = "폴더 이름",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkTheme) DarkTextTitle else TextCaption
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 입력창
            OutlinedTextField(
                value = folderName,
                onValueChange = {
                    if (it.length <= 20) { // 20자 제한
                        folderName = it
                        isDirty = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Left
                ),
                placeholder = {
                    AppText(
                        text = "ex) 양자역학",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            letterSpacing = 0.sp
                        ),
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isDarkTheme) TextCaption else Color.LightGray
                    )
                },
                singleLine = true,
                isError = isError,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) DarkGray600 else Color.Transparent,
                    unfocusedContainerColor = if (isDarkTheme) DarkGray600 else Color.Transparent,
                    errorContainerColor = if (isDarkTheme) DarkGray600 else Color.Transparent,
                    focusedTextColor = if (isDarkTheme) DarkTextTitle else Color.Unspecified,
                    unfocusedTextColor = if (isDarkTheme) DarkTextTitle else Color.Unspecified,
                    errorTextColor = if (isDarkTheme) DarkTextTitle else Color.Unspecified,
                    focusedBorderColor = if (isError) {
                        Negative
                    } else if (isDarkTheme) {
                        Color.Transparent
                    } else {
                        PrimaryNormal
                    },
                    unfocusedBorderColor = if (isError) {
                        Negative
                    } else if (isDarkTheme) {
                        Color.Transparent
                    } else {
                        Color.LightGray
                    },
                    errorBorderColor = Negative,
                    cursorColor = if (isError) Negative else PrimaryNormal
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. 상태 도움말 텍스트
            AppText(
                text = helperText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) {
                    Negative
                } else if (isDarkTheme) {
                    TextBody
                } else {
                    TextCaption
                },
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.brifeColors.ctaDisabled
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "취소",
                        color = Color.White,
                        style = PrimaryButtonTextStyle
                    )
                }

                // 저장 버튼
                Button(
                    onClick = { if (!isError && folderName.isNotBlank()) onSave(folderName) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNormal,
                        disabledContainerColor = PrimaryNormal.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isError && folderName.isNotBlank(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "저장",
                        color = Color.White,
                        style = PrimaryButtonTextStyle
                    )
                }
            }
        }
    }
}
