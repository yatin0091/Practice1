package com.software.pandit.lyftlaptopinterview.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindPhotoRepo(photoRepoImpl: PhotoRepoImpl): PhotoRepo

    @Binds
    fun bindPhotoPagingSourceFactory(
        factory: HiltPhotoPagingSourceFactory,
    ): PhotoPagingSourceFactory

}
