package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.software.pandit.lyftlaptopinterview.data.network.PhotoApi
import com.software.pandit.lyftlaptopinterview.data.network.NetworkModule
import com.software.pandit.lyftlaptopinterview.domain.PhotoMemoryCache
import javax.inject.Inject

class PhotoPagingSource @Inject constructor(
    val photoApi: PhotoApi,
    @NetworkModule.ApiKey val clientId: String,
    private val memoryCache: PhotoMemoryCache,
) : PagingSource<Int, Photo>() {

    private val seenIds = mutableSetOf<String>()

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Photo> {
        return try {
            if (params is LoadParams.Prepend) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = params.key
                )
            }

            val nextPageNumber = when (params) {
                is LoadParams.Refresh -> {
                    memoryCache.clear()
                    seenIds.clear()
                    params.key ?: 1
                }

                else -> params.key ?: 1
            }

            val cached = memoryCache.get(nextPageNumber)
            if (cached != null) {
                return LoadResult.Page(
                    data = cached,
                    prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                    nextKey = if (cached.isEmpty()) null else nextPageNumber + 1
                )
            }

            val photos = photoApi.getPhotos(nextPageNumber, clientId)

            // drop duplicates we've already emitted in this paging session
            val deduped = photos.filter { seenIds.add(it.id) }

            memoryCache.put(nextPageNumber, deduped)

            LoadResult.Page(
                data = deduped,
                nextKey = if (deduped.isEmpty()) null else nextPageNumber + 1,
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1
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