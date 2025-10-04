package com.software.pandit.lyftlaptopinterview.domain

import com.software.pandit.lyftlaptopinterview.data.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoMemoryCache @Inject constructor() {
    private val cachedPages = mutableMapOf<Int, List<Photo>>()

    fun get(page: Int): List<Photo>? = synchronized(cachedPages) { cachedPages[page] }

    fun put(page: Int, photos: List<Photo>) {
        synchronized(cachedPages) {
            cachedPages[page] = photos
        }
    }

    fun clear() {
        synchronized(cachedPages) { cachedPages.clear() }
    }
}
