package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.album.AlbumDataSource
import cord.eoeo.momentwo.data.album.AlbumRepositoryImpl
import cord.eoeo.momentwo.data.album.remote.AlbumRemoteDataSource
import cord.eoeo.momentwo.data.album.remote.AlbumService
import cord.eoeo.momentwo.domain.album.AlbumRepository
import cord.eoeo.momentwo.domain.album.GetAlbumListUseCase
import cord.eoeo.momentwo.domain.album.RequestCreateAlbumUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlbumModule {
    @Provides
    @Singleton
    fun provideAlbumService(retrofit: Retrofit): AlbumService = retrofit.create(AlbumService::class.java)

    @Provides
    @Singleton
    fun provideAlbumRemoteDataSource(albumService: AlbumService): AlbumDataSource = AlbumRemoteDataSource(albumService)

    @Provides
    @Singleton
    fun provideAlbumRepository(albumRemoteDataSource: AlbumDataSource): AlbumRepository = AlbumRepositoryImpl(albumRemoteDataSource)

    @Provides
    @Singleton
    fun provideRequestCreateAlbumUseCase(albumRepository: AlbumRepository): RequestCreateAlbumUseCase =
        RequestCreateAlbumUseCase(albumRepository)

    @Provides
    @Singleton
    fun provideGetAlbumListUseCase(albumRepository: AlbumRepository): GetAlbumListUseCase = GetAlbumListUseCase(albumRepository)
}
