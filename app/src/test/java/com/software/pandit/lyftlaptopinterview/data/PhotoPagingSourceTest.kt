package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.TestPhotoFactory
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoPagingSourceTest {

    private val photoA = TestPhotoFactory.photo(id = "A")
    private val photoB = TestPhotoFactory.photo(id = "B")
    private val photoC = TestPhotoFactory.photo(id = "C")

    @Test
    fun `refresh drops duplicate ids and advances to next key`() = runTest {
        val api = RecordingPhotoApi(
            pages = mapOf(
                1 to listOf(photoA, photoB, TestPhotoFactory.photo(id = "B"))
            )
        )
        val pagingSource = PhotoPagingSource(api, clientId = "client")

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 30,
                placeholdersEnabled = false
            )
        )

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(2)
        assertThat(page.data).containsExactly(photoA, photoB).inOrder()
        assertThat(api.requests)
            .containsExactly(RecordingPhotoApi.Request(page = 1, clientId = "client"))
    }

    @Test
    fun `append filters ids previously emitted`() = runTest {
        val api = RecordingPhotoApi(
            pages = mapOf(
                1 to listOf(photoA, photoB),
                2 to listOf(TestPhotoFactory.photo(id = "B"), photoC)
            )
        )
        val pagingSource = PhotoPagingSource(api, clientId = "client")

        // load first page to seed seen ids
        pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 30,
                placeholdersEnabled = false
            )
        )

        val append = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 30,
                placeholdersEnabled = false
            )
        )

        val appendPage = append as PagingSource.LoadResult.Page
        assertThat(appendPage.prevKey).isNull()
        assertThat(appendPage.nextKey).isEqualTo(3)
        assertThat(appendPage.data).containsExactly(photoC)
        assertThat(api.requests.map { it.page }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `load returns error result when api throws`() = runTest {
        val failure = IllegalStateException("boom")
        val pagingSource = PhotoPagingSource(
            photoApi = RecordingPhotoApi(error = failure),
            clientId = "client"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 30,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val loadError = result as PagingSource.LoadResult.Error
        assertThat(loadError.throwable).isEqualTo(failure)
    }

    @Test
    fun `getRefreshKey returns key closest to anchor`() {
        val pagingSource = PhotoPagingSource(RecordingPhotoApi(), clientId = "client")
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(photoA),
                    prevKey = null,
                    nextKey = 2
                ),
                PagingSource.LoadResult.Page(
                    data = listOf(photoB),
                    prevKey = 2,
                    nextKey = 4
                )
            ),
            anchorPosition = 1,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(state)

        assertThat(refreshKey).isEqualTo(3)
    }

    private class RecordingPhotoApi(
        pages: Map<Int, List<Photo>> = emptyMap(),
        private val error: Throwable? = null
    ) : PhotoApi {
        private val responses = pages.toMutableMap()
        val requests = mutableListOf<Request>()

        override suspend fun getPhotos(page: Int, clientId: String): List<Photo> {
            requests += Request(page, clientId)
            error?.let { throw it }
            return responses[page].orEmpty()
        }

        data class Request(val page: Int, val clientId: String)
    }
}
