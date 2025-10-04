package com.software.pandit.lyftlaptopinterview

import com.software.pandit.lyftlaptopinterview.data.CurrentUserCollection
import com.software.pandit.lyftlaptopinterview.data.Links
import com.software.pandit.lyftlaptopinterview.data.LinksX
import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.data.ProfileImage
import com.software.pandit.lyftlaptopinterview.data.Urls

fun createPhoto(id: String) = Photo(
    blur_hash = "hash",
    color = "#fff",
    created_at = "2020-01-01T00:00:00Z",
    current_user_collections = listOf(
        CurrentUserCollection(
            cover_photo = Any(),
            id = 1,
            last_collected_at = "2020-01-01T00:00:00Z",
            published_at = "2020-01-01T00:00:00Z",
            title = "title",
            updated_at = "2020-01-01T00:00:00Z",
            user = Any()
        )
    ),
    description = "desc",
    height = 100,
    id = id,
    liked_by_user = false,
    likes = 0,
    links = Links(
        download = "download",
        download_location = "download_location",
        html = "html",
        self = "self"
    ),
    updated_at = "2020-01-02T00:00:00Z",
    urls = Urls(
        full = "full",
        raw = "raw",
        regular = "regular",
        small = "small",
        thumb = "thumb"
    ),
    width = 100
)

fun createLinksX() = LinksX(
    html = "html",
    likes = "likes",
    photos = "photos",
    portfolio = "portfolio",
    self = "self"
)

fun createProfileImage() = ProfileImage(
    large = "large",
    medium = "medium",
    small = "small"
)
