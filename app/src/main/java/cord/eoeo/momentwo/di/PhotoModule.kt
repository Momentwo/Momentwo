package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.MomentwoDatabase
import cord.eoeo.momentwo.data.photo.PhotoDataSource
import cord.eoeo.momentwo.data.photo.PhotoRemoteMediator
import cord.eoeo.momentwo.data.photo.PhotoRepository
import cord.eoeo.momentwo.data.photo.PhotoRepositoryImpl
import cord.eoeo.momentwo.data.photo.local.PhotoDao
import cord.eoeo.momentwo.data.photo.local.PhotoLocalDataSource
import cord.eoeo.momentwo.data.photo.local.PhotoRemoteKeyDao
import cord.eoeo.momentwo.data.photo.remote.PhotoRemoteDataSource
import cord.eoeo.momentwo.data.photo.remote.PhotoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PhotoModule {
    @Provides
    @Singleton
    fun providePhotoDao(database: MomentwoDatabase): PhotoDao = database.photoDao()

    @Provides
    @Singleton
    fun providePhotoRemoteKeyDao(database: MomentwoDatabase): PhotoRemoteKeyDao = database.photoRemoteKeyDao()

    @Provides
    @Singleton
    fun providePhotoService(retrofit: Retrofit): PhotoService = retrofit.create(PhotoService::class.java)

    @Provides
    @Singleton
    @QualifierModule.RemoteDataSource
    fun providePhotoRemoteDataSource(photoService: PhotoService): PhotoDataSource.Remote =
        PhotoRemoteDataSource(photoService)

    @Provides
    @Singleton
    @QualifierModule.LocalDataSource
    fun providePhotoLocalDataSource(
        photoDao: PhotoDao,
        photoRemoteKeyDao: PhotoRemoteKeyDao,
    ): PhotoDataSource.Local = PhotoLocalDataSource(photoDao, photoRemoteKeyDao)

    @Provides
    @Singleton
    fun providePhotoRemoteMediator(
        @QualifierModule.RemoteDataSource photoRemoteDataSource: PhotoDataSource.Remote,
        @QualifierModule.LocalDataSource photoLocalDataSource: PhotoDataSource.Local,
    ): PhotoRemoteMediator = PhotoRemoteMediator(photoRemoteDataSource, photoLocalDataSource)

    @Provides
    @Singleton
    fun providePhotoRepository(
        @QualifierModule.RemoteDataSource photoRemoteDataSource: PhotoDataSource.Remote,
        @QualifierModule.LocalDataSource photoLocalDataSource: PhotoDataSource.Local,
        photoRemoteMediator: PhotoRemoteMediator,
    ): PhotoRepository = PhotoRepositoryImpl(photoRemoteDataSource, photoLocalDataSource, photoRemoteMediator)
}
