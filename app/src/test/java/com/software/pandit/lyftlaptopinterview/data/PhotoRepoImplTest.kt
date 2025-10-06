package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.MainDispatcherRule
import com.software.pandit.lyftlaptopinterview.TestPhotoFactory
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepoImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photo = TestPhotoFactory.photo(id = "from-api")

    @Test
    fun `getPhotos emits paging data provided by paging source`() = runTest {
        val provider = RecordingProvider { RecordingPagingSource(listOf(photo)) }
        val repo = PhotoRepoImpl(provider)
        val differ = differ()

        val job = launch {
            repo.getPhotos().collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        assertThat(provider.invocations).isEqualTo(1)
        assertThat(provider.lastPagingSource?.refreshCalls).isEqualTo(1)
        assertThat(differ.snapshot()).containsExactly(photo)

        job.cancel()
    }

    @Test
    fun `provider is invoked lazily when flow is collected`() = runTest {
        val provider = RecordingProvider { RecordingPagingSource(listOf(photo)) }
        val repo = PhotoRepoImpl(provider)

        assertThat(provider.invocations).isEqualTo(0)

        val job = launch { repo.getPhotos().collectLatest { /* no-op */ } }
        advanceUntilIdle()

        assertThat(provider.invocations).isEqualTo(1)

        job.cancel()
    }

    @Test
    fun `new collectors receive fresh paging sources`() = runTest {
        val provider = RecordingProvider { RecordingPagingSource(emptyList()) }
        val repo = PhotoRepoImpl(provider)
        val flow = repo.getPhotos()

        val firstCollector = launch { flow.collectLatest { /* no-op */ } }
        advanceUntilIdle()
        assertThat(provider.invocations).isEqualTo(1)
        firstCollector.cancel()

        val secondCollector = launch { flow.collectLatest { /* no-op */ } }
        advanceUntilIdle()
        assertThat(provider.invocations).isEqualTo(2)

        secondCollector.cancel()
    }

    private fun differ(): AsyncPagingDataDiffer<Photo> = AsyncPagingDataDiffer(
        diffCallback = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        },
        updateCallback = NoopListCallback,
        mainDispatcher = mainDispatcherRule.dispatcher,
        workerDispatcher = mainDispatcherRule.dispatcher
    )

    private class RecordingProvider(
        private val pagingSourceFactory: () -> RecordingPagingSource
    ) : Provider<PhotoPagingSource> {
        var invocations = 0
            private set
        var lastPagingSource: RecordingPagingSource? = null
            private set

        override fun get(): PhotoPagingSource {
            invocations++
            return pagingSourceFactory().also { lastPagingSource = it }
        }
    }

    private class RecordingPagingSource(
        private val items: List<Photo>
    ) : PhotoPagingSource(
        photoApi = object : PhotoApi {
            override suspend fun getPhotos(page: Int, clientId: String): List<Photo> = items
        },
        clientId = "client"
    ) {
        var refreshCalls = 0
            private set

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
            if (params is LoadParams.Refresh) {
                refreshCalls++
            }
            return LoadResult.Page(
                data = items,
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
