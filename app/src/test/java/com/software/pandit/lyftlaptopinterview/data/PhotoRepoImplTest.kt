package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.MainDispatcherRule
import com.software.pandit.lyftlaptopinterview.TestPhotoFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import com.software.pandit.lyftlaptopinterview.domain.PhotoMemoryCache

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepoImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `getPhotos collects paging data from provided paging source`() = runTest {
        val factory = CountingFactory { RecordingPagingSource() }
        val repo = PhotoRepoImpl(factory, PhotoMemoryCache())

        val pagingData = repo.getPhotos().first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<Photo>() {
                override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
            },
            updateCallback = NoopListCallback,
            mainDispatcher = mainDispatcherRule.dispatcher,
            workerDispatcher = mainDispatcherRule.dispatcher
        )

        differ.submitData(pagingData)
        advanceUntilIdle()
        assertThat(factory.invocations).isEqualTo(1)
        assertThat(factory.lastPagingSource.refreshInvocations).isEqualTo(1)
        assertThat(differ.snapshot()).containsExactly(
            TestPhotoFactory.photo(id = "from-paging-source")
        )
    }

    private class CountingFactory(
        private val delegate: () -> RecordingPagingSource
    ) : PhotoPagingSourceFactory {
        var invocations = 0
            private set
        lateinit var lastPagingSource: RecordingPagingSource
            private set

        override fun create(): PhotoPagingSource {
            invocations++
            return delegate().also { lastPagingSource = it }
        }
    }

    private class RecordingPagingSource : PhotoPagingSource(
        photoApi = object : com.software.pandit.lyftlaptopinterview.data.network.PhotoApi {
            override suspend fun getPhotos(page: Int, clientId: String): List<Photo> =
                listOf(TestPhotoFactory.photo(id = "from-paging-source"))
        },
        clientId = "client",
        memoryCache = PhotoMemoryCache(),
    ) {
        var refreshInvocations = 0
            private set

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
            if (params is LoadParams.Refresh) {
                refreshInvocations++
            }
            return LoadResult.Page(
                data = listOf(TestPhotoFactory.photo(id = "from-paging-source")),
                prevKey = null,
                nextKey = null
            )
        }

        override fun getRefreshKey(state: PagingState<Int, Photo>): Int? = null
    }

    private object NoopListCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}
