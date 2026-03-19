package com.example.brife.feature.archive.component

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
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.Negative
import com.example.brife.ui.theme.TextCaption
import com.example.brife.ui.theme.TextTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
    currentFolderCount: Int = 1, // 현재 폴더 개수 전달받음
    existingFolders: List<String> = listOf("즐겨찾기") // 기존 폴더 리스트
) {
    val sheetState = rememberModalBottomSheetState()
    var folderName by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) } // 사용자가 입력을 시도했는지 여부

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
                text = "새 폴더 만들기",
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
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD5D8DC)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "취소",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                // 저장 버튼
                Button(
                    onClick = { if (!isError && folderName.isNotBlank()) onSave(folderName) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
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
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
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