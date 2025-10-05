package com.software.pandit.lyftlaptopinterview

import com.software.pandit.lyftlaptopinterview.data.CurrentUserCollection
import com.software.pandit.lyftlaptopinterview.data.Links
import com.software.pandit.lyftlaptopinterview.data.LinksX
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.ProfileImage
import com.software.pandit.lyftlaptopinterview.data.Urls

/**
 * Lightweight factory helpers for constructing photos and nested models in tests without
 * repeating the huge amount of Unsplash payload boilerplate in every file.
 */
object TestPhotoFactory {

    private val coverPhotoStub = Any()
    private val userStub = Any()

    fun photo(
        id: String,
        description: String? = "desc",
        likes: Int = 0,
        blurHash: String = "hash"
    ): Photo = Photo(
        blur_hash = blurHash,
        color = "#fff",
        created_at = "2020-01-01T00:00:00Z",
        current_user_collections = listOf(
            CurrentUserCollection(
                cover_photo = coverPhotoStub,
                id = 1,
                last_collected_at = "2020-01-01T00:00:00Z",
                published_at = "2020-01-01T00:00:00Z",
                title = "title",
                updated_at = "2020-01-01T00:00:00Z",
                user = userStub
            )
        ),
        description = description,
        height = 100,
        id = id,
        liked_by_user = false,
        likes = likes,
        links = links(),
        updated_at = "2020-01-02T00:00:00Z",
        urls = urls(),
        width = 100
    )

    fun links(): Links = Links(
        download = "download",
        download_location = "download_location",
        html = "html",
        self = "self"
    )

    fun urls(): Urls = Urls(
        full = "full",
        raw = "raw",
        regular = "regular",
        small = "small",
        thumb = "thumb"
    )

    fun linksX(): LinksX = LinksX(
        html = "html",
        likes = "likes",
        photos = "photos",
        portfolio = "portfolio",
        self = "self"
    )

    fun profileImage(): ProfileImage = ProfileImage(
        large = "large",
        medium = "medium",
        small = "small"
    )
}
