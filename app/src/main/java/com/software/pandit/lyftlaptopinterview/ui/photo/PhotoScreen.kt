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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.software.pandit.lyftlaptopinterview.data.Photo

@Composable
fun PhotoRoute(modifier: Modifier = Modifier, viewModel: PhotoVm = hiltViewModel()) {
    val items = viewModel.photos.collectAsLazyPagingItems()
    when (val s = items.loadState.refresh) {
        is LoadState.Loading -> {
            Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        is LoadState.Error -> {
            Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Failed to load photos: ${s.error.localizedMessage ?: "Unknown error"}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { items.retry() }) { Text("Retry") }
                }
            }
            return
        }

        else -> Unit
    }
    PhotoListScreen(modifier, items)
}

@Composable
fun PhotoListScreen(
    modifier: Modifier,
    items: LazyPagingItems<Photo>,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = items.itemCount,
            key = { idx -> items[idx]?.id ?: "placeholder-$idx" }
        ) { idx ->
            val photo = items[idx]
            if (photo == null) {
                // placeholder while paging loads
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                PhotoRow(photo, onClick = { })
            }
        }

        // Append state footer
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
}

@Composable
private fun PhotoRow(photo: Photo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Replace with your image loader (Coil/Glide)
        // AsyncImage(model = photo.url, contentDescription = null, modifier = Modifier.size(72.dp))
        AsyncImage(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            model = photo.urls.small,
            contentDescription = photo.id
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(photo.id, style = MaterialTheme.typography.titleMedium)
            Text(photo.description ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}