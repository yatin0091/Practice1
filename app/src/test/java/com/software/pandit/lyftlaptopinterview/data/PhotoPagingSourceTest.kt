package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.createPhoto
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoPagingSourceTest {

    private val photosForFirstPage = listOf(
        createPhoto(id = "1"),
        createPhoto(id = "2"),
        createPhoto(id = "2")
    )

    private val photosForSecondPage = listOf(
        createPhoto(id = "2"),
        createPhoto(id = "3")
    )

    @Test
    fun `load returns page with deduped photos`() = runTest {
        val fakeApi = QueuePhotoApi(
            mutableListOf(photosForFirstPage.toMutableList())
        )
        val pagingSource = PhotoPagingSource(fakeApi, "client")

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).containsExactly(
            photosForFirstPage[0],
            photosForFirstPage[1]
        ).inOrder()
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(2)
    }

    @Test
    fun `load filters duplicates seen across requests`() = runTest {
        val fakeApi = QueuePhotoApi(
            mutableListOf(
                photosForFirstPage.toMutableList(),
                photosForSecondPage.toMutableList()
            )
        )
        val pagingSource = PhotoPagingSource(fakeApi, "client")

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
        assertThat(page.data).containsExactly(photosForSecondPage[1])
    }

    @Test
    fun `load returns error when api throws`() = runTest {
        val fakeApi = object : PhotoApi {
            override suspend fun getPhotos(page: Int, clientId: String): List<Photo> {
                throw IllegalStateException("boom")
            }
        }
        val pagingSource = PhotoPagingSource(fakeApi, "client")

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
    fun `getRefreshKey returns closest page key`() {
        val pagingSource = PhotoPagingSource(QueuePhotoApi(mutableListOf()), "client")
        val pagingState = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(createPhoto("1")),
                    prevKey = null,
                    nextKey = 2
                ),
                PagingSource.LoadResult.Page(
                    data = listOf(createPhoto("2")),
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

    private class QueuePhotoApi(
        private val responses: MutableList<MutableList<Photo>>
    ) : PhotoApi {
        override suspend fun getPhotos(page: Int, clientId: String): List<Photo> {
            if (responses.isEmpty()) error("No response configured for page $page")
            return responses.removeAt(0)
        }
    }
}
