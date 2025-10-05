package com.software.pandit.lyftlaptopinterview.domain

import com.software.pandit.lyftlaptopinterview.data.Photo
import com.software.pandit.lyftlaptopinterview.domain.model.PhotoSummary

internal fun Photo.toSummary(): PhotoSummary = PhotoSummary(
    id = id,
    title = description?.takeIf { it.isNotBlank() } ?: "Photo #$id",
    description = description,
    likes = likes,
    width = width,
    height = height,
    thumbnailUrl = urls.small,
    fullImageUrl = urls.full,
    accentColorHex = color.takeIf { it.isNotBlank() }
)
