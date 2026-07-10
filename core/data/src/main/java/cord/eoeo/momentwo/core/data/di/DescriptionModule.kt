package cord.eoeo.momentwo.core.data.di

import cord.eoeo.momentwo.core.data.description.DescriptionDataSource
import cord.eoeo.momentwo.core.data.description.DescriptionRepository
import cord.eoeo.momentwo.core.data.description.DescriptionRepositoryImpl
import cord.eoeo.momentwo.core.data.description.remote.DescriptionRemoteDataSource
import cord.eoeo.momentwo.core.data.description.remote.DescriptionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DescriptionModule {
    @Provides
    @Singleton
    fun provideDescriptionService(retrofit: Retrofit): DescriptionService =
        retrofit.create(DescriptionService::class.java)

    @Provides
    @Singleton
    fun provideDescriptionRemoteDataSource(descriptionService: DescriptionService): DescriptionDataSource =
        DescriptionRemoteDataSource(descriptionService)

    @Provides
    @Singleton
    fun provideDescriptionRepository(descriptionRemoteDataSource: DescriptionDataSource): DescriptionRepository =
        DescriptionRepositoryImpl(descriptionRemoteDataSource)
}
