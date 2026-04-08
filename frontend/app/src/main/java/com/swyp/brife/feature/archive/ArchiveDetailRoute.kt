package com.swyp.brife.feature.archive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.ArchiveRepository
import com.swyp.brife.feature.widget.WidgetActionReceiver
import com.swyp.brife.feature.widget.WidgetRefreshHelper

@Composable
fun ArchiveDetailRoute(
    archiveId: Long,
    folderName: String,
    onBackClick: () -> Unit,
    onNewsClick: ((ArchiveNewsItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authStorage = remember { AuthLocalStorage(context) }
    val repository = remember {
        ArchiveRepository(
            api = NetworkModule.archiveApiService,
            newsApi = NetworkModule.newsApiService,
            authLocalStorage = AuthLocalStorage(context)
        )
    }
    val isFavorite = folderName == "즐겨찾기"
    val viewModel: ArchiveDetailViewModel = viewModel(
        key = "archive_detail_$archiveId",
        factory = ArchiveDetailViewModelFactory(archiveId, isFavorite, repository)
    )
    val newsItems by viewModel.newsItems.collectAsState()

    ArchiveDetailScreen(
        folderName = folderName,
        newsItems = newsItems,
        onBackClick = onBackClick,
        onDeleteItems = { selectedIds ->
            viewModel.deleteItems(selectedIds) { deletedNewsIds ->
                if (isFavorite && deletedNewsIds.isNotEmpty()) {
                    deletedNewsIds.forEach { newsId ->
                        WidgetActionReceiver.removeBookmarkedId(context, newsId)
                    }
                    WidgetRefreshHelper.refreshAll(context)
                }
            }
        },
        onNewsClick = onNewsClick,
        modifier = modifier
    )
}
