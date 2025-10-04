package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.software.pandit.lyftlaptopinterview.data.network.NetworkModule
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import javax.inject.Inject

class PhotoPagingSource @Inject constructor(
    val photoApi: PhotoApi,
    @NetworkModule.ApiKey val clientId: String
) : PagingSource<Int, Photo>() {

    private val seenIds = mutableSetOf<String>()

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Photo> {
        return try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 1
            val photos = photoApi.getPhotos(nextPageNumber, clientId)

            // drop duplicates we've already emitted in this paging session
            val deduped = photos.filter { seenIds.add(it.id) }

            LoadResult.Page(
                nextKey = nextPageNumber + 1,
                data = deduped,
                prevKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}