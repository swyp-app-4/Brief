package com.example.brife.feature.archive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.ArchiveRepository

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
    val viewModel: ArchiveDetailViewModel = viewModel(
        key = "archive_detail_$archiveId",
        factory = ArchiveDetailViewModelFactory(archiveId, repository)
    )
    val newsItems by viewModel.newsItems.collectAsState()

    ArchiveDetailScreen(
        folderName = folderName,
        newsItems = newsItems,
        onBackClick = onBackClick,
        onDeleteItems = { selectedIds ->
            viewModel.deleteItems(selectedIds)
        },
        onNewsClick = onNewsClick,
        modifier = modifier
    )
}
