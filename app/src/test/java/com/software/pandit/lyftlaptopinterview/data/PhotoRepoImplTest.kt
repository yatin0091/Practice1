package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
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
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepoImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `getPhotos collects paging data from provided paging source`() = runTest {
        val provider = CountingProvider { RecordingPagingSource() }
        val repo = PhotoRepoImpl(provider)

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
        assertThat(provider.invocations).isEqualTo(1)
        assertThat(provider.lastPagingSource.refreshInvocations).isEqualTo(1)
        assertThat(differ.snapshot()).containsExactly(
            TestPhotoFactory.photo(id = "from-paging-source")
        )
    }

    private class CountingProvider(
        private val delegate: () -> RecordingPagingSource
    ) : Provider<PhotoPagingSource> {
        var invocations = 0
            private set
        lateinit var lastPagingSource: RecordingPagingSource
            private set

        override fun get(): PhotoPagingSource {
            invocations++
            return delegate().also { lastPagingSource = it }
        }
    }

    private class RecordingPagingSource : PhotoPagingSource(
        photoApi = object : com.software.pandit.lyftlaptopinterview.data.network.PhotoApi {
            override suspend fun getPhotos(page: Int, clientId: String): List<Photo> =
                listOf(TestPhotoFactory.photo(id = "from-paging-source"))
        },
        clientId = "client"
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
