package com.software.pandit.lyftlaptopinterview.ui.photo

data class PhotoScreenState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hasContent: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && !hasContent && errorMessage == null
}
