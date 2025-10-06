package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.software.pandit.lyftlaptopinterview.domain.PhotoMemoryCache
import com.software.pandit.lyftlaptopinterview.domain.model.PhotoSummary
import com.software.pandit.lyftlaptopinterview.domain.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PhotoRepo {
    fun getPhotos(): Flow<PagingData<PhotoSummary>>
}

class PhotoRepoImpl @Inject constructor(
    private val pagingSourceFactory: PhotoPagingSourceFactory,
    private val memoryCache: PhotoMemoryCache,
) : PhotoRepo {

    override fun getPhotos(): Flow<PagingData<PhotoSummary>> {
        val pageSize = 10

        return Pager(
            PagingConfig(
                pageSize = pageSize,
                prefetchDistance = 2,
                enablePlaceholders = false,
                maxSize = pageSize * 3
            )
        ) {
            pagingSourceFactory.create().apply { registerInvalidatedCallback { memoryCache.clear() } }
        }.flow.map { pagingData ->
            pagingData.map { it.toSummary() }
        }
    }

}
