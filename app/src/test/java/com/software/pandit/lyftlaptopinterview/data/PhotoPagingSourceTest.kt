package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.TestPhotoFactory
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoPagingSourceTest {

    private val firstPage = listOf(
        TestPhotoFactory.photo(id = "1"),
        TestPhotoFactory.photo(id = "2"),
        TestPhotoFactory.photo(id = "2")
    )

    private val secondPage = listOf(
        TestPhotoFactory.photo(id = "2"),
        TestPhotoFactory.photo(id = "3"),
        TestPhotoFactory.photo(id = "4")
    )

    @Test
    fun `refresh load returns page with duplicates removed`() = runTest {
        val pagingSource = pagingSourceWith(firstPage)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).containsExactly(
            firstPage[0],
            firstPage[1]
        ).inOrder()
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(2)
    }

    @Test
    fun `append load omits ids seen in previous loads`() = runTest {
        val pagingSource = pagingSourceWith(firstPage, secondPage)

        pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).containsExactly(secondPage[2])
        assertThat(page.nextKey).isEqualTo(3)
    }

    @Test
    fun `load wraps api failure in LoadResult_Error`() = runTest {
        val pagingSource = PhotoPagingSource(
            photoApi = object : PhotoApi {
                override suspend fun getPhotos(page: Int, clientId: String): List<Photo> {
                    throw IllegalStateException("boom")
                }
            },
            clientId = "client"
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }

    @Test
    fun `getRefreshKey picks closest page to anchor`() {
        val pagingSource = pagingSourceWith()
        val pagingState = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(TestPhotoFactory.photo(id = "1")),
                    prevKey = null,
                    nextKey = 2
                ),
                PagingSource.LoadResult.Page(
                    data = listOf(TestPhotoFactory.photo(id = "2")),
                    prevKey = 2,
                    nextKey = 4
                )
            ),
            anchorPosition = 1,
            config = androidx.paging.PagingConfig(
                pageSize = 10,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(pagingState)

        assertThat(refreshKey).isEqualTo(3)
    }

    private fun pagingSourceWith(vararg pages: List<Photo>) = PhotoPagingSource(
        photoApi = QueuePhotoApi(pages.toList()),
        clientId = "client"
    )

    private class QueuePhotoApi(
        pages: List<List<Photo>>
    ) : PhotoApi {
        private val responses = ArrayDeque(pages)

        override suspend fun getPhotos(page: Int, clientId: String): List<Photo> {
            if (responses.isEmpty()) error("No response configured for page $page")
            return responses.removeFirst()
        }
    }
}
