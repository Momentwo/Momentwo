package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.subalbum.SubAlbumDataSource
import cord.eoeo.momentwo.data.subalbum.SubAlbumRepository
import cord.eoeo.momentwo.data.subalbum.SubAlbumRepositoryImpl
import cord.eoeo.momentwo.data.subalbum.remote.SubAlbumRemoteDataSource
import cord.eoeo.momentwo.data.subalbum.remote.SubAlbumService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubAlbumModule {
    @Provides
    @Singleton
    fun provideSubAlbumService(retrofit: Retrofit): SubAlbumService = retrofit.create(SubAlbumService::class.java)

    @Provides
    @Singleton
    fun provideSubAlbumRemoteDataSource(subAlbumService: SubAlbumService): SubAlbumDataSource = SubAlbumRemoteDataSource(subAlbumService)

    @Provides
    @Singleton
    fun provideSubAlbumRepository(subAlbumRemoteDataSource: SubAlbumDataSource): SubAlbumRepository =
        SubAlbumRepositoryImpl(subAlbumRemoteDataSource)
}
