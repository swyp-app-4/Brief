package com.swyp.brife.feature.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.brife.R
import com.swyp.brife.data.model.ArchiveFolderResponse
import com.swyp.brife.data.model.ArchiveItemResponse
import com.swyp.brife.feature.archive.component.CreateFolderBottomSheet
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BorderDefault
import com.swyp.brife.ui.theme.ComponentDefault
import com.swyp.brife.ui.theme.CtaActive
import com.swyp.brife.ui.theme.CtaDisabled
import com.swyp.brife.ui.theme.InterestSelectedLight
import com.swyp.brife.ui.theme.Negative
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.PrimaryButtonTextStyle
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextSubtitle

private const val SPECIAL_CHAR_ONLY_ERROR = "SPECIAL_CHAR_ONLY"
private const val SEARCH_QUERY_MAX_LENGTH = 20

private enum class ArchiveSearchSortFilter(val label: String) {
    ALL("전체"),
    LATEST("최신순"),
    OLDEST("오래된순")
}

@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    folders: List<ArchiveFolderUiModel>,
    favoriteArchiveId: Long = 0L,
    favoriteItemCount: Int = 0,
    searchQuery: String = "",
    recentSearchQueries: List<String> = emptyList(),
    isSearchLoading: Boolean = false,
    searchFolders: List<ArchiveFolderResponse> = emptyList(),
    searchItems: List<ArchiveItemResponse> = emptyList(),
    searchNewsItems: List<ArchiveNewsItem> = emptyList(),
    hasSearchCompleted: Boolean = false,
    searchErrorMessage: String? = null,
    onFolderAdd: (String) -> Unit,
    onNavigateToDetail: (archiveId: Long, folderName: String) -> Unit,
    isDeleteMode: Boolean = false,
    selectedFolderIds: Set<Long> = emptySet(),
    onToggleFolderSelect: (Long) -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    isRenameMode: Boolean = false,
    onFolderRename: (archiveId: Long, newName: String) -> Unit = { _, _ -> },
    onCancelRename: () -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActivate: () -> Unit = {},
    onSearchDeactivate: () -> Unit = {},
    onSearchNewsClick: (ArchiveNewsItem) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchClear: () -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    onRecentSearchClick: (String) -> Unit = {},
    onRecentSearchRemove: (String) -> Unit = {},
    onRecentSearchClearAll: () -> Unit = {},
    showTopBar: Boolean = true
) {
    val folderCount = folders.size + 1
    val isSelectionMode = isDeleteMode || isRenameMode
    val selectionGuideText = when {
        isDeleteMode -> "삭제할 폴더를 선택해주세요"
        isRenameMode -> "이름을 수정할 폴더를 선택해주세요"
        else -> ""
    }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showRenameSheet by remember { mutableStateOf(false) }
    var renamingFolderName by remember { mutableStateOf("") }
    var renamingFolderId by remember { mutableStateOf(0L) }
    val showSearchMode = isSearchActive && !isSelectionMode
    var searchSortFilter by remember { mutableStateOf(ArchiveSearchSortFilter.ALL) }

    Box(modifier = modifier.fillMaxSize()) {
        if (showSearchMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                ArchiveSearchTopBar(
                    searchQuery = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    onBackClick = {
                        searchSortFilter = ArchiveSearchSortFilter.ALL
                        onSearchClear()
                        onSearchDeactivate()
                    },
                    onClearQuery = {
                        searchSortFilter = ArchiveSearchSortFilter.ALL
                        onSearchClear()
                    },
                    onSearchSubmit = onSearchSubmit
                )
                val trimmedSearchQuery = searchQuery.trim()
                when {
                    trimmedSearchQuery.isBlank() -> {
                        ArchiveRecentSearches(
                            recentQueries = recentSearchQueries,
                            onRecentQueryClick = onRecentSearchClick,
                            onDeleteRecentQuery = onRecentSearchRemove,
                            onClearAllRecentQueries = onRecentSearchClearAll
                        )
                    }
                    isSearchLoading -> ArchiveSearchLoading(modifier = Modifier.weight(1f))
                    searchErrorMessage == SPECIAL_CHAR_ONLY_ERROR -> {
                        ArchiveSearchStateBody(
                            text = "특수문자를 제외한\n검색어로 검색해주세요.",
                            backgroundImageRes = R.drawable.img_explore_error,
                            characterImageRes = R.drawable.img_explore_error_character,
                            backgroundModifier = Modifier
                                .size(75.dp)
                                .offset(x = (70).dp, y = (-50).dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    searchErrorMessage != null -> {
                        ArchiveSearchStateBody(
                            text = "연결이 원활하지 않아요.\n잠시 후 다시 시도해주세요.",
                            backgroundImageRes = R.drawable.img_explore_network_error,
                            characterImageRes = R.drawable.img_explore_network_error_character,
                            backgroundModifier = Modifier
                                .size(100.dp)
                                .offset(x = (60).dp, y = (-40).dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    hasSearchCompleted && searchNewsItems.isNotEmpty() -> {
                        ArchiveSearchResultsBody(
                            query = trimmedSearchQuery,
                            items = searchNewsItems,
                            selectedFilter = searchSortFilter,
                            onFilterSelected = { searchSortFilter = it },
                            onNewsClick = onSearchNewsClick,
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                    hasSearchCompleted -> {
                        ArchiveSearchStateBody(
                            text = "보관한 기사 중에 없어요.\n다른 제목으로 찾거나, 탐색에서 저장해보세요.",
                            backgroundImageRes = R.drawable.img_explore_empty,
                            characterImageRes = R.drawable.img_explore_empty_character,
                            backgroundModifier = Modifier
                                .size(60.dp)
                                .offset(x = (-60).dp, y = (-10).dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

            Spacer(modifier = Modifier.height(20.dp))

            if (isSelectionMode) {
                ArchiveSelectionModeBanner(
                    text = selectionGuideText,
                    onCancelClick = {
                        if (isDeleteMode) onCancelDelete() else onCancelRename()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!isSelectionMode) {
                ArchiveSearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSearchActivate
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            AppText(
                text = buildAnnotatedString {
                    append("폴더 ")
                    withStyle(style = SpanStyle(color = PrimaryNormal)) {
                        append("$folderCount")
                    }
                    append("개")
                },
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArchiveFolderCard(
                    modifier = Modifier.weight(1f),
                    isSelectionMode = isSelectionMode,
                    enabled = !isSelectionMode,
                    onClick = { if (!isSelectionMode) showCreateSheet = true }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_folderadd),
                            contentDescription = "폴더 추가",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                ArchiveFolderCard(
                    modifier = Modifier.weight(1f),
                    isSelectionMode = isSelectionMode,
                    enabled = !isSelectionMode,
                    onClick = {
                        if (!isSelectionMode) {
                            onNavigateToDetail(favoriteArchiveId, "즐겨찾기")
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.TopStart),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AppText(
                                text = "즐겨찾기",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            AppText(
                                text = "${favoriteItemCount}개",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextBody
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "즐겨찾기",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }

            folders.chunked(2).forEach { rowFolders ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowFolders.forEach { folder ->
                        ArchiveFolderCard(
                            modifier = Modifier.weight(1f),
                            selected = isDeleteMode && folder.archiveId in selectedFolderIds,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                when {
                                    isDeleteMode -> onToggleFolderSelect(folder.archiveId)
                                    isRenameMode -> {
                                        renamingFolderName = folder.folderName
                                        renamingFolderId = folder.archiveId
                                        showRenameSheet = true
                                    }
                                    else -> onNavigateToDetail(folder.archiveId, folder.folderName)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.TopStart),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AppText(
                                        text = folder.folderName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    AppText(
                                        text = "${folder.itemCount}개",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextBody
                                    )
                                }

                                if (isRenameMode) {
                                    AppText(
                                        text = "선택",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryNormal,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    )
                                }
                            }
                        }
                    }
                    if (rowFolders.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isDeleteMode) {
                Spacer(modifier = Modifier.height(80.dp))
            }
            }
        }

        if (isDeleteMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancelDelete,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "취소",
                        color = Color.White,
                        style = PrimaryButtonTextStyle
                    )
                }

                Button(
                    onClick = onConfirmDelete,
                    enabled = selectedFolderIds.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Negative,
                        disabledContainerColor = Negative.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "삭제",
                        color = Color.White,
                        style = PrimaryButtonTextStyle
                    )
                }
            }
        }

        if (showCreateSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showCreateSheet = false },
                onSave = { name ->
                    onFolderAdd(name)
                    showCreateSheet = false
                },
                currentFolderCount = folderCount,
                existingFolders = folders.map { it.folderName } + "즐겨찾기"
            )
        }

        if (showRenameSheet) {
            CreateFolderBottomSheet(
                title = "폴더명 수정",
                initialFolderName = renamingFolderName,
                onDismissRequest = {
                    showRenameSheet = false
                    onCancelRename()
                },
                onSave = { newName ->
                    onFolderRename(renamingFolderId, newName)
                    showRenameSheet = false
                    onCancelRename()
                },
                currentFolderCount = 0,
                existingFolders = (folders.map { it.folderName } + "즐겨찾기")
                    .filter { it != renamingFolderName }
            )
        }
    }
}

@Composable
private fun ArchiveSearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearQuery: () -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified
            )
        }
        Spacer(modifier = Modifier.width(4.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = ComponentDefault,
                    shape = RoundedCornerShape(999.dp)
                )
                .border(
                    width = 1.6.dp,
                    color = TextCaption,
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_explore_search),
                contentDescription = null,
                tint = TextCaption,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    if (newQuery.length <= SEARCH_QUERY_MAX_LENGTH) {
                        onQueryChange(newQuery)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSubtitle),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                singleLine = true,
                cursorBrush = SolidColor(CtaActive),
                decorationBox = { innerTextField ->
                    Box {
                        if (searchQuery.isEmpty()) {
                            AppText(
                                text = "저장한 기사 제목을 입력해주세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextCaption
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore_recent_clear),
                    contentDescription = "검색어 초기화",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClearQuery() }
                )
            }
        }
    }
}

@Composable
private fun ArchiveSearchResultsBody(
    query: String,
    items: List<ArchiveNewsItem>,
    selectedFilter: ArchiveSearchSortFilter,
    onFilterSelected: (ArchiveSearchSortFilter) -> Unit,
    onNewsClick: (ArchiveNewsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedItems = remember(items, selectedFilter) {
        when (selectedFilter) {
            ArchiveSearchSortFilter.ALL -> items
            ArchiveSearchSortFilter.LATEST -> items.sortedByDescending { it.time }
            ArchiveSearchSortFilter.OLDEST -> items.sortedBy { it.time }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArchiveSearchSortFilter.values().forEach { filter ->
                ArchiveSearchFilterChip(
                    text = filter.label,
                    selected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }

        AppText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = CtaActive)) { append(query) }
                append(" 관련 뉴스")
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextSubtitle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        sortedItems.forEach { item ->
            ArchiveNewsCard(
                item = item,
                onClick = { onNewsClick(item) }
            )
        }
    }
}

@Composable
private fun ArchiveSearchFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppText(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = TextBody,
        modifier = modifier
            .background(
                color = if (selected) InterestSelectedLight else ComponentDefault,
                shape = RoundedCornerShape(30.dp)
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) PrimaryNormal else ComponentDefault,
                shape = RoundedCornerShape(30.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun ArchiveSearchLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = CtaActive,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun ArchiveSearchStateBody(
    text: String,
    backgroundImageRes: Int,
    characterImageRes: Int,
    backgroundModifier: Modifier = Modifier.size(100.dp),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = null,
                modifier = backgroundModifier
            )
            Image(
                painter = painterResource(id = characterImageRes),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppText(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = TextCaption,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ArchiveRecentSearches(
    recentQueries: List<String>,
    onRecentQueryClick: (String) -> Unit,
    onDeleteRecentQuery: (String) -> Unit,
    onClearAllRecentQueries: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "최근 검색어",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSubtitle
            )
            AppText(
                text = "전체 삭제",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
                modifier = Modifier.clickable { onClearAllRecentQueries() }
            )
        }

        recentQueries.forEach { query ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecentQueryClick(query) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore_recent),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                AppText(
                    text = query,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCaption,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore_clear),
                    contentDescription = "삭제",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDeleteRecentQuery(query) }
                )
            }
        }
    }
}

@Composable
private fun ArchiveSearchBar(
    query: String = "",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = ComponentDefault,
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.6.dp,
                color = TextCaption,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_explore_search),
            contentDescription = null,
            tint = TextCaption,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AppText(
            text = "저장한 기사 제목을 입력해주세요",
            style = MaterialTheme.typography.bodyMedium,
            color = TextCaption,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ArchiveSelectionModeBanner(
    text: String,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = InterestSelectedLight,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onCancelClick) {
            AppText(
                text = "취소",
                style = PrimaryButtonTextStyle,
                color = PrimaryNormal
            )
        }
    }
}

@Composable
fun ArchiveFolderCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isSelectionMode: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> InterestSelectedLight
                isSelectionMode -> ComponentDefault.copy(alpha = 0.95f)
                else -> ComponentDefault
            }
        ),
        border = when {
            selected -> BorderStroke(1.dp, PrimaryNormal)
            isSelectionMode -> BorderStroke(1.dp, BorderDefault)
            else -> null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}
