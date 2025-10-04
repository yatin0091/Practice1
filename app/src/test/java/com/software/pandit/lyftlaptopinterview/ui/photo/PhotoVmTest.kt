package com.software.pandit.lyftlaptopinterview.ui.photo

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.MainDispatcherRule
import com.software.pandit.lyftlaptopinterview.createPhoto
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.PhotoRepo
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoVmTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepo = FakePhotoRepo()

    @Test
    fun `photos flow emits data from repo`() = runTest {
        val vm = PhotoVm(fakeRepo)
        val photo = createPhoto("1")
        fakeRepo.emit(PagingData.from(listOf(photo)))

        val differ = AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<Photo>() {
                override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
            },
            updateCallback = NoopListCallback,
            mainDispatcher = mainDispatcherRule.dispatcher,
            workerDispatcher = mainDispatcherRule.dispatcher
        )

        vm.photos.test {
            val pagingData = awaitItem()
            differ.submitData(pagingData)
            advanceUntilIdle()
            assertThat(differ.snapshot()).containsExactly(photo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private object NoopListCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private class FakePhotoRepo : PhotoRepo {
        private val sharedFlow = MutableSharedFlow<PagingData<Photo>>(replay = 1)

        override fun getPhotos(): Flow<PagingData<Photo>> = sharedFlow.asSharedFlow()

        suspend fun emit(data: PagingData<Photo>) {
            sharedFlow.emit(data)
        }
    }
}
