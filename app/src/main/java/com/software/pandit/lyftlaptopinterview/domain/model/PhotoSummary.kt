package com.software.pandit.lyftlaptopinterview.domain.model

/**
 * Domain-level representation of a photo tailored for presentation.
 */
data class PhotoSummary(
    val id: String,
    val title: String,
    val description: String?,
    val likes: Int,
    val width: Int,
    val height: Int,
    val thumbnailUrl: String,
    val fullImageUrl: String,
    val accentColorHex: String?,
) {
    val aspectRatio: Float = if (height == 0) 1f else width.toFloat() / height
}
