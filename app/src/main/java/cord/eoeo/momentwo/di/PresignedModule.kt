package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.presigned.PresignedDataSource
import cord.eoeo.momentwo.data.presigned.remote.PresignedRemoteDataSource
import cord.eoeo.momentwo.data.presigned.remote.PresignedService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresignedModule {
    @Provides
    @Singleton
    fun providePresignedService(retrofit: Retrofit): PresignedService = retrofit.create(PresignedService::class.java)

    @Provides
    @Singleton
    fun providePresignedRemoteDataSource(presignedService: PresignedService): PresignedDataSource =
        PresignedRemoteDataSource(presignedService)
}
