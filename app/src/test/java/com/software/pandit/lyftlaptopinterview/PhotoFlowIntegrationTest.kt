package com.software.pandit.lyftlaptopinterview

import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.TestPhotoFactory
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.PhotoPagingSource
import com.software.pandit.lyftlaptopinterview.data.PhotoRepoImpl
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import com.software.pandit.lyftlaptopinterview.ui.photo.PhotoVm
import java.util.ArrayDeque
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoFlowIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `photo vm exposes deduped photos from repo and paging source`() = runTest {
        val photo1 = TestPhotoFactory.photo(id = "1")
        val photo2 = TestPhotoFactory.photo(id = "2")
        val duplicatePhoto2 = TestPhotoFactory.photo(id = "2")
        val duplicateAcrossPages = TestPhotoFactory.photo(id = "2")
        val photo3 = TestPhotoFactory.photo(id = "3")

        val api = QueuePhotoApi(
            pages = listOf(
                listOf(
                    photo1,
                    photo2,
                    duplicatePhoto2
                ),
                listOf(
                    duplicateAcrossPages,
                    photo3
                )
            )
        )
        val repo = PhotoRepoImpl(Provider { PhotoPagingSource(api, clientId = "client") })
        val viewModel = PhotoVm(repo)

        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<Photo>() {
                override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
            },
            updateCallback = NoopListCallback,
            mainDispatcher = mainDispatcherRule.dispatcher,
            workerDispatcher = mainDispatcherRule.dispatcher
        )

        val pagingData = viewModel.photos.first()
        differ.submitData(pagingData)
        advanceUntilIdle()

        // Trigger append load for the second page
        differ[2]
        advanceUntilIdle()

        assertThat(api.requestedPages).containsExactly(1, 2)
        assertThat(differ.snapshot()).containsExactly(
            photo1,
            photo2,
            photo3
        ).inOrder()
    }

    private object NoopListCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private class QueuePhotoApi(
        pages: List<List<Photo>>
    ) : PhotoApi {
        private val responses = ArrayDeque(pages)
        val requestedPages = mutableListOf<Int>()

        override suspend fun getPhotos(page: Int, clientId: String): List<Photo> {
            requestedPages += page
            if (responses.isEmpty()) error("No response configured for page $page")
            return responses.removeFirst()
        }
    }
}
