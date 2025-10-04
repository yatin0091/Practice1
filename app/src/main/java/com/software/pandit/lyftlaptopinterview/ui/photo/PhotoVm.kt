package com.software.pandit.lyftlaptopinterview.ui.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.cachedIn
import com.software.pandit.lyftlaptopinterview.domain.GetCuratedPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PhotoVm @Inject constructor(
    getCuratedPhotos: GetCuratedPhotosUseCase
) : ViewModel() {

    val photos = getCuratedPhotos().cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(PhotoScreenState())
    val uiState: StateFlow<PhotoScreenState> = _uiState.asStateFlow()

    fun onLoadStatesChanged(loadStates: CombinedLoadStates) {
        val refresh = loadStates.refresh
        val isLoading = refresh is LoadState.Loading && !_uiState.value.hasContent
        val errorMessage = when (refresh) {
            is LoadState.Error -> refresh.error.localizedMessage ?: "Unable to load photos."
            else -> null
        }

        _uiState.update { current ->
            current.copy(
                isLoading = isLoading,
                errorMessage = if (current.hasContent) null else errorMessage
            )
        }
    }

    fun onPhotoCountChanged(count: Int) {
        _uiState.update { current ->
            current.copy(
                hasContent = count > 0,
                isLoading = if (count > 0) false else current.isLoading,
                errorMessage = if (count > 0) null else current.errorMessage
            )
        }
    }

}