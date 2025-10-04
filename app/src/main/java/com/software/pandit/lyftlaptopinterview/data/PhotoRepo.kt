package com.software.pandit.lyftlaptopinterview.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Provider

interface PhotoRepo {
    fun getPhotos(): Flow<PagingData<Photo>>
}

class PhotoRepoImpl @Inject constructor(
    private val pagingSourceProvider: Provider<PhotoPagingSource>
) : PhotoRepo {

    override fun getPhotos(): Flow<PagingData<Photo>> = Pager(
        PagingConfig(
            pageSize = 10,
            prefetchDistance = 2,
            enablePlaceholders = false
        )
    ) {
        pagingSourceProvider.get()
    }.flow

}