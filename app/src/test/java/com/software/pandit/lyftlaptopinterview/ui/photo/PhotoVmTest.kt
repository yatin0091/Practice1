package com.software.pandit.lyftlaptopinterview.ui.photo

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.MainDispatcherRule
import com.software.pandit.lyftlaptopinterview.TestPhotoFactory
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.PhotoRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoVmTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `photos relays paging data from repository`() = runTest {
        val repo = RecordingPhotoRepo()
        val vm = PhotoVm(repo)
        val differ = differ()
        val expectedPhoto = TestPhotoFactory.photo(id = "1")

        val job = launch {
            vm.photos.collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        repo.emit(PagingData.from(listOf(expectedPhoto)))
        advanceUntilIdle()

        assertThat(differ.snapshot()).containsExactly(expectedPhoto)

        job.cancel()
    }

    @Test
    fun `photos caches subscription to upstream repository`() = runTest {
        val repo = RecordingPhotoRepo()
        val vm = PhotoVm(repo)
        val cachedPhoto = TestPhotoFactory.photo(id = "cached")

        val firstCollector = launch { vm.photos.collectLatest { } }
        repo.emit(PagingData.from(listOf(cachedPhoto)))
        advanceUntilIdle()
        firstCollector.cancel()

        vm.photos.test {
            val pagingData = awaitItem()
            val differ = differ()
            differ.submitData(pagingData)
            advanceUntilIdle()
            assertThat(differ.snapshot()).containsExactly(cachedPhoto)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(repo.subscriptionCount).isEqualTo(1)
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

    private object NoopListCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private class RecordingPhotoRepo : PhotoRepo {
        private val upstream = MutableSharedFlow<PagingData<Photo>>(replay = 1)
        var subscriptionCount = 0
            private set

        override fun getPhotos(): Flow<PagingData<Photo>> = upstream
            .onSubscription { subscriptionCount++ }
            .asSharedFlow()

        suspend fun emit(data: PagingData<Photo>) {
            upstream.emit(data)
        }
    }
}
