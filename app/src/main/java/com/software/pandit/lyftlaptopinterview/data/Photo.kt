package com.software.pandit.lyftlaptopinterview.data

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Photo(
    val blur_hash: String,
    val color: String,
    val created_at: String,
    val current_user_collections: List<CurrentUserCollection>,
    val description: String?,
    val height: Int,
    val id: String,
    val liked_by_user: Boolean,
    val likes: Int,
    val links: Links,
    val updated_at: String?,
    val urls: Urls,
    val width: Int
)

@JsonClass(generateAdapter = true)
data class CurrentUserCollection(
    val cover_photo: Any,
    val id: Int,
    val last_collected_at: String,
    val published_at: String,
    val title: String,
    val updated_at: String,
    val user: Any
)

@JsonClass(generateAdapter = true)
data class Links(
    val download: String,
    val download_location: String,
    val html: String,
    val self: String
)

@JsonClass(generateAdapter = true)
data class Urls(
    val full: String,
    val raw: String,
    val regular: String,
    val small: String,
    val thumb: String
)

@JsonClass(generateAdapter = true)
data class LinksX(
    val html: String,
    val likes: String,
    val photos: String,
    val portfolio: String,
    val self: String
)

@JsonClass(generateAdapter = true)
data class ProfileImage(
    val large: String,
    val medium: String,
    val small: String
)