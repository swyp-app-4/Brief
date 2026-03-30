package com.example.brife.feature.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.AppTopBar2
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.*

private val INQUIRY_TYPES = listOf(
    "뉴스 / 콘텐츠 관련",
    "계정 / 로그인 문제",
    "앱 기능 / 사용 문의",
    "제안 / 피드백",
    "기타"
)

// 문의 유형별 수신 이메일 매핑 — 이 곳에서만 관리
private val INQUIRY_EMAIL_MAP = mapOf(
    "뉴스 / 콘텐츠 관련" to "a",
    "계정 / 로그인 문제" to "b",
    "앱 기능 / 사용 문의" to "c",
    "제안 / 피드백" to "d",
    "기타" to "d"
)

private fun inquiryEmailFor(inquiryType: String): String =
    INQUIRY_EMAIL_MAP[inquiryType] ?: "d"

private fun sendInquiryEmail(
    context: Context,
    inquiryType: String,
    name: String,
    senderEmail: String,
    subject: String,
    content: String
) {
    val targetEmail = inquiryEmailFor(inquiryType)
    val body = buildString {
        appendLine("문의 유형: $inquiryType")
        appendLine("이름: $name")
        appendLine("이메일: $senderEmail")
        appendLine()
        appendLine(content)
    }
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(targetEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "이메일 앱 선택"))
    }
}

// ─────────────────────────────────────────────────────────────
// 메인 화면
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneToOneInquiryScreen(
    isLoggedIn: Boolean = false,
    // API에서 비동기로 내려오는 초기값 (로그인 유저 전용)
    initialName: String = "",
    initialEmail: String = "",
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedInquiryType by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // API 응답이 도착하면 (initialName/initialEmail 변경 시) 폼 필드에 반영
    // name은 로그인 유저도 수정 가능하므로, 아직 입력하지 않은 경우에만 덮어씀
    LaunchedEffect(initialName) {
        if (isLoggedIn && name.isEmpty() && initialName.isNotEmpty()) {
            name = initialName
        }
    }
    LaunchedEffect(initialEmail) {
        if (isLoggedIn && initialEmail.isNotEmpty()) {
            email = initialEmail
        }
    }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPrivacyChecked by remember { mutableStateOf(false) }
    var showInquiryTypeSheet by remember { mutableStateOf(false) }
    var showCompletedSheet by remember { mutableStateOf(false) }
    // 등록 버튼 클릭 후에만 에러 표시
    var showValidationErrors by remember { mutableStateOf(false) }

    val isFormValid = selectedInquiryType.isNotBlank() &&
            name.isNotBlank() &&
            email.isNotBlank() &&
            subject.isNotBlank() &&
            content.isNotBlank() &&
            isPrivacyChecked

    val inquiryTypeError = showValidationErrors && selectedInquiryType.isBlank()
    val nameError = showValidationErrors && name.isBlank()
    val emailError = showValidationErrors && email.isBlank()
    val subjectError = showValidationErrors && subject.isBlank()
    val contentError = showValidationErrors && content.isBlank()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            AppTopBar2(
                title = "문의 등록",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                PrimaryButton(
                    text = "문의 등록하기",
                    onClick = {
                        if (isFormValid) {
                            showCompletedSheet = true
                        } else {
                            showValidationErrors = true
                        }
                    },
                    enabled = isFormValid
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ① 문의 유형
            InquiryFieldSection(label = "문의 유형") {
                InquiryTypeField(
                    selectedType = selectedInquiryType,
                    isError = inquiryTypeError,
                    onClick = { showInquiryTypeSheet = true }
                )
                if (inquiryTypeError) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppText(
                            text = "문의 유형을 입력해주세요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Negative
                        )
                    }
                }
            }

            // ② 이름 (로그인 유저는 nickname 초기값, 수정 가능)
            InquiryFieldSection(label = "이름") {
                InquiryInputField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "이름을 입력해주세요",
                    isError = nameError,
                    errorMessage = "이름을 입력해주세요.",
                    readOnly = false
                )
            }

            // ③ 이메일 (로그인 유저는 read-only, 비로그인은 직접 입력)
            InquiryFieldSection(label = "이메일") {
                InquiryInputField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "문의 답장을 받으실 이메일을 입력해주세요",
                    isError = emailError,
                    errorMessage = "이메일을 입력해주세요.",
                    readOnly = isLoggedIn
                )
            }

            // ④ 제목 (최대 30자 + 글자 수 표시)
            InquiryFieldSection(label = "제목") {
                InquiryInputField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = "문의 제목을 입력해주세요",
                    isError = subjectError,
                    errorMessage = "제목을 입력해주세요.",
                    maxLength = 30,
                    showCharCount = true
                )
            }

            // ⑤ 내용 (최대 1000자, 높이 5.5배 + 글자 수 표시)
            InquiryFieldSection(label = "내용") {
                InquiryInputField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = "문의 할 내용을 입력해주세요",
                    isError = contentError,
                    errorMessage = "내용을 입력해주세요.",
                    minLines = 7,
                    maxLines = 7,
                    maxLength = 1000,
                    showCharCount = true
                )
            }

            // ⑥ 개인정보 수집 및 이용 동의 (약관동의 화면 동일 스타일)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPrivacyChecked = !isPrivacyChecked }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = if (isPrivacyChecked) R.drawable.ic_aftercheck else R.drawable.ic_beforecheck
                    ),
                    contentDescription = if (isPrivacyChecked) "동의됨" else "동의 안됨",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                AppText(
                    text = buildAnnotatedString {
                        append("개인정보 수집 및 이용에 동의합니다 ")
                        withStyle(style = SpanStyle(color = Negative)) {
                            append("(필수)")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtitle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showInquiryTypeSheet) {
        InquiryTypeBottomSheet(
            selectedType = selectedInquiryType,
            onTypeSelected = { selectedInquiryType = it },
            onDismissRequest = { showInquiryTypeSheet = false }
        )
    }

    if (showCompletedSheet) {
        InquiryCompletedBottomSheet(
            onDismissRequest = { showCompletedSheet = false },
            onConfirmClick = {
                sendInquiryEmail(
                    context = context,
                    inquiryType = selectedInquiryType,
                    name = name,
                    senderEmail = email,
                    subject = subject,
                    content = content
                )
                showCompletedSheet = false
                onBackClick()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 항목 라벨 + 콘텐츠 래퍼
// ─────────────────────────────────────────────────────────────

@Composable
private fun InquiryFieldSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppText(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        content()
    }
}

// ─────────────────────────────────────────────────────────────
// 공통 텍스트 입력 필드
// 아카이브 폴더명 입력칸과 동일한 OutlinedTextField 스타일
// ─────────────────────────────────────────────────────────────

@Composable
private fun InquiryInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    errorMessage: String,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxLength: Int = Int.MAX_VALUE,
    showCharCount: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            AppText(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = TextBody
            )
        },
        readOnly = readOnly,
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // 읽기 전용 필드는 포커스해도 테두리 색상 유지
            focusedBorderColor = when {
                readOnly -> BorderStrong
                isError -> Negative
                else -> PrimaryNormal
            },
            unfocusedBorderColor = if (isError) Negative else BorderStrong,
            errorBorderColor = Negative,
            cursorColor = if (isError) Negative else PrimaryNormal,
            disabledBorderColor = BorderStrong
        )
    )

    // 에러 메시지 / 글자 수 카운터
    if (isError || showCharCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isError) {
                AppText(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = Negative
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (showCharCount) {
                AppText(
                    text = "${value.length}/$maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 문의 유형 드롭다운 트리거 필드
// 아카이브 폴더명 입력칸과 동일한 외형, 클릭 시 바텀시트 오픈
// ─────────────────────────────────────────────────────────────

@Composable
private fun InquiryTypeField(
    selectedType: String,
    isError: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isError) Negative else BorderStrong

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = if (selectedType.isBlank()) "문의 유형을 선택해 주세요." else selectedType,
            style = MaterialTheme.typography.bodySmall,
            color = if (selectedType.isBlank()) TextBody else TextSubtitle,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_onetoone_fold),
            contentDescription = "문의 유형 선택",
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 문의 유형 선택 바텀시트
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InquiryTypeBottomSheet(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // 확정 전 임시 선택 상태 — "선택 완료" 클릭 전까지 반영되지 않음
    var tempSelected by remember { mutableStateOf(selectedType) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 40.dp)
        ) {
            AppText(
                text = "문의 유형을 선택해 주세요",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            INQUIRY_TYPES.forEach { type ->
                val isChecked = tempSelected == type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 토글 로직: 이미 체크되어 있으면 해제(""), 아니면 선택(type)
                            tempSelected = if (isChecked) "" else type
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isChecked) R.drawable.ic_aftercheck else R.drawable.ic_beforecheck
                        ),
                        contentDescription = if (isChecked) "선택됨" else "선택 안됨",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AppText(
                        text = type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtitle
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "취소",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Button(
                    onClick = {
                        // 유형 미선택 시 닫히지 않음
                        if (tempSelected.isNotBlank()) {
                            onTypeSelected(tempSelected)
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNormal),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "선택 완료",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 접수 완료 바텀시트
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InquiryCompletedBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                AppText(
                    text = "1:1 문의가 정상적으로 접수되었습니다.",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppText(
                    text = "문의하신 내용은 이메일로 발송되며,\n답변까지 시간이 다소 소요될 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(25.dp))

                PrimaryButton(
                    text = "확인",
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Image(
                painter = painterResource(id = R.drawable.illust4_login),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(200.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "1. 비로그인 - 기본 상태")
@Composable
private fun OneToOneInquiryGuestPreview() {
    BrifeTheme {
        OneToOneInquiryScreen(isLoggedIn = false)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. 로그인 - 이름/이메일 채워진 상태")
@Composable
private fun OneToOneInquiryLoggedInPreview() {
    BrifeTheme {
        OneToOneInquiryScreen(
            isLoggedIn = true,
            initialName = "홍길동",
            initialEmail = "brife@example.com"
        )
    }
}
