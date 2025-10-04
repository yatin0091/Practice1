package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepoImplTest {

    @Test
    fun `getPhotos emits paging data and uses provider`() = runTest {
        val pagingSource = FakePagingSource()
        val provider = object : Provider<PhotoPagingSource> {
            var invocationCount = 0
            override fun get(): PhotoPagingSource {
                invocationCount++
                return pagingSource
            }
        }
        val repo = PhotoRepoImpl(provider)

        val pagingData = repo.getPhotos().first()

        assertThat(provider.invocationCount).isEqualTo(1)
        assertThat(pagingData).isInstanceOf(PagingData::class.java)
    }

    private class FakePagingSource : PhotoPagingSource(
        photoApi = object : com.software.pandit.lyftlaptopinterview.data.network.PhotoApi {
            override suspend fun getPhotos(page: Int, clientId: String): List<Photo> = emptyList()
        },
        clientId = "client"
    ) {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
            return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        }

        override fun getRefreshKey(state: PagingState<Int, Photo>): Int? = null
    }
}
