package com.software.pandit.lyftlaptopinterview.data

import javax.inject.Inject
import javax.inject.Provider

fun interface PhotoPagingSourceFactory {
    fun create(): PhotoPagingSource
}

class HiltPhotoPagingSourceFactory @Inject constructor(
    private val pagingSourceProvider: Provider<PhotoPagingSource>,
) : PhotoPagingSourceFactory {

    override fun create(): PhotoPagingSource = pagingSourceProvider.get()
}
