package cord.eoeo.momentwo.core.data.di
import cord.eoeo.momentwo.core.common.dispatcher.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher

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
import cord.eoeo.momentwo.core.data.photo.PhotoRepository
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
    fun providePhotoRemoteDataSource(
        photoService: PhotoService,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): PhotoDataSource.Remote =
        PhotoRemoteDataSource(photoService, dispatcher)

    @Provides
    @Singleton
    @QualifierModule.LocalDataSource
    fun providePhotoLocalDataSource(
        photoDao: PhotoDao,
        photoRemoteKeyDao: PhotoRemoteKeyDao,
        @IoDispatcher dispatcher: CoroutineDispatcher,
        database: MomentwoDatabase,
    ): PhotoDataSource.Local = PhotoLocalDataSource(photoDao, photoRemoteKeyDao, dispatcher, database)

    @Provides
    @Singleton
    fun providePhotoRemoteMediator(
        @QualifierModule.RemoteDataSource photoRemoteDataSource: PhotoDataSource.Remote,
        @QualifierModule.LocalDataSource photoLocalDataSource: PhotoDataSource.Local,
    ): PhotoRemoteMediator.Factory = PhotoRemoteMediator.Factory(photoRemoteDataSource, photoLocalDataSource)

    @Provides @Singleton
    fun providePhotoRepository(impl: cord.eoeo.momentwo.core.data.library.AccountPhotoRepository): PhotoRepository = impl
}
