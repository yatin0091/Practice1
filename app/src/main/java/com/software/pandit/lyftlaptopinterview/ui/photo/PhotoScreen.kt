package com.software.pandit.lyftlaptopinterview.ui.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.software.pandit.lyftlaptopinterview.domain.model.PhotoSummary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PhotoRoute(modifier: Modifier = Modifier, viewModel: PhotoVm = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val items = viewModel.photos.collectAsLazyPagingItems()

    LaunchedEffect(viewModel, items) {
        snapshotFlow { items.loadState }
            .distinctUntilChanged()
            .collectLatest { loadStates -> viewModel.onLoadStatesChanged(loadStates) }
    }

    LaunchedEffect(items.itemCount) {
        viewModel.onPhotoCountChanged(items.itemCount)
    }

    when {
        uiState.isLoading -> {
            Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            PhotoError(
                modifier = modifier,
                message = uiState.errorMessage,
                onRetry = { items.retry() }
            )
        }

        uiState.isEmpty -> {
            Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No photos yet.")
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { items.refresh() }) { Text("Refresh") }
                }
            }
        }

        else -> PhotoListScreen(modifier, items)
    }
}

@Composable
fun PhotoListScreen(
    modifier: Modifier,
    items: LazyPagingItems<PhotoSummary>,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.id },
            contentType = items.itemContentType { "photo" }
        ) { idx ->
            when (val photo = items[idx]) {
                null -> LoadingPlaceholder()
                else -> PhotoRow(photo, onClick = { })
            }
        }

        // Append state footer
        appendLoadState(items)
    }
}

@Composable
private fun PhotoRow(photo: PhotoSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            model = photo.thumbnailUrl,
            contentDescription = photo.title
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(photo.title, style = MaterialTheme.typography.titleMedium)
            Text(photo.description ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${photo.likes} likes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun PhotoError(modifier: Modifier, message: String, onRetry: () -> Unit) {
    Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Failed to load photos: $message")
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

private fun LazyListScope.appendLoadState(items: LazyPagingItems<PhotoSummary>) {
    when (val s = items.loadState.append) {
        is LoadState.Loading -> {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                }
            }
        }

        is LoadState.Error -> {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("More photos failed to load.")
                    TextButton(onClick = { items.retry() }) { Text("Retry") }
                }
            }
        }

        else -> Unit
    }
}
