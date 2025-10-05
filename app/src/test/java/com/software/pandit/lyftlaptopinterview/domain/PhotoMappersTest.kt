package com.software.pandit.lyftlaptopinterview.domain

import com.google.common.truth.Truth.assertThat
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.Urls
import org.junit.Test

class PhotoMappersTest {

    @Test
    fun `maps network photo to presentation summary`() {
        val photo = Photo(
            blur_hash = "abc",
            color = "#FFFFFF",
            created_at = "2024-01-01",
            current_user_collections = emptyList(),
            description = "Snowy mountains",
            height = 2000,
            id = "photo-1",
            liked_by_user = false,
            likes = 42,
            links = com.software.pandit.lyftlaptopinterview.data.Links(
                download = "download",
                download_location = "location",
                html = "html",
                self = "self"
            ),
            updated_at = null,
            urls = Urls(
                full = "full-url",
                raw = "raw",
                regular = "regular",
                small = "small",
                thumb = "thumb"
            ),
            width = 3000
        )

        val summary = photo.toSummary()

        assertThat(summary.id).isEqualTo("photo-1")
        assertThat(summary.title).isEqualTo("Snowy mountains")
        assertThat(summary.description).isEqualTo("Snowy mountains")
        assertThat(summary.thumbnailUrl).isEqualTo("small")
        assertThat(summary.fullImageUrl).isEqualTo("full-url")
        assertThat(summary.accentColorHex).isEqualTo("#FFFFFF")
        assertThat(summary.aspectRatio).isWithin(0.001f).of(1.5f)
    }

    @Test
    fun `falls back to generated title when description blank`() {
        val photo = Photo(
            blur_hash = "abc",
            color = "",
            created_at = "2024-01-01",
            current_user_collections = emptyList(),
            description = " ",
            height = 100,
            id = "42",
            liked_by_user = false,
            likes = 0,
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
            width = 100
        )

        val summary = photo.toSummary()

        assertThat(summary.title).isEqualTo("Photo #42")
        assertThat(summary.accentColorHex).isNull()
    }
}
