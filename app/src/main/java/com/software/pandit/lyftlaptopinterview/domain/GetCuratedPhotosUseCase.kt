package com.software.pandit.lyftlaptopinterview.domain

import androidx.paging.PagingData
import com.software.pandit.lyftlaptopinterview.data.PhotoRepo
import com.software.pandit.lyftlaptopinterview.domain.model.PhotoSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCuratedPhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepo
) {
    operator fun invoke(): Flow<PagingData<PhotoSummary>> = photoRepository.getPhotos()
}
