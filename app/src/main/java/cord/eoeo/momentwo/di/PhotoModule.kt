package cord.eoeo.momentwo.di

import android.content.Context
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.data.photo.PhotoDataSource
import cord.eoeo.momentwo.core.data.photo.PhotoRemoteMediator
import cord.eoeo.momentwo.core.data.photo.PhotoRepositoryImpl
import cord.eoeo.momentwo.core.database.PhotoDao
import cord.eoeo.momentwo.core.data.photo.local.PhotoLocalDataSource
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyDao
import cord.eoeo.momentwo.core.data.photo.remote.PhotoRemoteDataSource
import cord.eoeo.momentwo.core.data.photo.remote.PhotoService
import cord.eoeo.momentwo.core.data.presigned.PresignedDataSource
import cord.eoeo.momentwo.domain.photo.DownloadPhotoUseCase
import cord.eoeo.momentwo.core.data.photo.PhotoRepository
import cord.eoeo.momentwo.domain.photo.UpdateIsLikedUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        presignedRemoteDataSource: PresignedDataSource,
        photoRemoteMediator: PhotoRemoteMediator,
        @ApplicationContext applicationContext: Context,
    ): PhotoRepository = PhotoRepositoryImpl(
        photoRemoteDataSource,
        photoLocalDataSource,
        presignedRemoteDataSource,
        photoRemoteMediator,
        applicationContext,
    )

    @Provides
    @Singleton
    fun provideDownloadPhotoUseCase(photoRepository: PhotoRepository): DownloadPhotoUseCase =
        DownloadPhotoUseCase(photoRepository)

    @Provides
    @Singleton
    fun provideUpdateIsLikedUseCase(photoRepository: PhotoRepository): UpdateIsLikedUseCase =
        UpdateIsLikedUseCase(photoRepository)
}
