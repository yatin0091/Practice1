package com.software.pandit.lyftlaptopinterview.domain

import com.software.pandit.lyftlaptopinterview.data.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoMemoryCache @Inject constructor() {

    data class CachedPage(
        val photos: List<Photo>,
        val endOfPaginationReached: Boolean,
    )

    private val cachedPages = mutableMapOf<Int, CachedPage>()

    fun get(page: Int): CachedPage? = synchronized(cachedPages) { cachedPages[page] }

    fun put(page: Int, photos: List<Photo>, endOfPaginationReached: Boolean) {
        synchronized(cachedPages) {
            cachedPages[page] = CachedPage(
                photos = photos,
                endOfPaginationReached = endOfPaginationReached,
            )
        }
    }

    fun clear() {
        synchronized(cachedPages) { cachedPages.clear() }
    }
}
