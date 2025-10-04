package com.software.pandit.lyftlaptopinterview.domain

import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.Urls
import org.junit.Test

class PhotoMemoryCacheTest {

    private val cache = PhotoMemoryCache()

    @Test
    fun `returns cached page without clearing`() {
        val page = 1
        val photos = listOf(
            Photo(
                blur_hash = "hash",
                color = "#000000",
                created_at = "2024-01-01",
                current_user_collections = emptyList(),
                description = "desc",
                height = 10,
                id = "id",
                liked_by_user = false,
                likes = 1,
                links = com.software.pandit.lyftlaptopinterview.data.Links(
                    download = "download",
                    download_location = "location",
                    html = "html",
                    self = "self"
                ),
                updated_at = null,
                urls = Urls(
                    full = "full",
                    raw = "raw",
                    regular = "regular",
                    small = "small",
                    thumb = "thumb"
                ),
                width = 10
            )
        )

        cache.put(page, photos)

        assertThat(cache.get(page)).containsExactlyElementsIn(photos)
    }

    @Test
    fun `clear removes previously cached pages`() {
        cache.put(2, emptyList())
        cache.clear()

        assertThat(cache.get(2)).isNull()
    }
}
