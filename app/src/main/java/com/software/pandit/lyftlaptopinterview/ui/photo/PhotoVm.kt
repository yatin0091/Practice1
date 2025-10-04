package com.software.pandit.lyftlaptopinterview.ui.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.software.pandit.lyftlaptopinterview.data.PhotoRepo
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PhotoVm @Inject constructor(val photoRepo: PhotoRepo) : ViewModel() {

    val photos = photoRepo.getPhotos().cachedIn(viewModelScope)

}