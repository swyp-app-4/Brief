package com.swyp.brife.feature.archive.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.PrimaryButtonTextStyle
import com.swyp.brife.ui.theme.Negative
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextTitle

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
        containerColor = Color.White,
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
                style = MaterialTheme.typography.titleLarge,
                color = TextTitle
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2. 라벨
            AppText(
                text = "폴더 이름",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption
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
                placeholder = { AppText(text = "ex) 양자역학", color = Color.LightGray) },
                singleLine = true,
                isError = isError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) Negative else PrimaryNormal,
                    unfocusedBorderColor = if (isError) Negative else Color.LightGray,
                    errorBorderColor = Negative,
                    cursorColor = if (isError) Negative else PrimaryNormal
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. 상태 도움말 텍스트
            AppText(
                text = helperText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) Negative else TextCaption,
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
                        containerColor = Color(0xFFD5D8DC)
                    ),
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
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

// Preview 5. 폴더명 수정 바텀시트 (기존 폴더명 초기값)
@Preview(showBackground = true, name = "5. 폴더명 수정 바텀시트")
@Composable
fun RenameFolderBottomSheetPreview() {
    BrifeTheme {
        CreateFolderBottomSheet(
            onDismissRequest = {},
            onSave = {},
            title = "폴더명 수정",
            initialFolderName = "경제 공부",
            existingFolders = listOf("즐겨찾기", "IT 트렌드") // 수정 대상 본인 제외
        )
    }
}

@Preview(showBackground = true, name = "1. 기본 상태")
@Composable
fun DefaultPreview() {
    BrifeTheme {
        CreateFolderBottomSheet(onDismissRequest = {}, onSave = {})
    }
}

@Preview(showBackground = true, name = "2. 에러 상태(특수문자)")
@Composable
fun ErrorPreview() {
    BrifeTheme {
        // 내부 로직 확인을 위해 Surface 위에서 호출
        Surface(color = Color.White) {
            CreateFolderBottomSheet(
                onDismissRequest = {},
                onSave = {},
                existingFolders = listOf("양자역학")
            )
        }
    }
}
