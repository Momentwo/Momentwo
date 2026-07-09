package cord.eoeo.momentwo.core.data.di

import cord.eoeo.momentwo.core.data.like.LikeDataSource
import cord.eoeo.momentwo.core.data.like.LikeRepository
import cord.eoeo.momentwo.core.data.like.LikeRepositoryImpl
import cord.eoeo.momentwo.core.data.like.remote.LikeRemoteDataSource
import cord.eoeo.momentwo.core.data.like.remote.LikeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LikeModule {
    @Provides
    @Singleton
    fun provideLikeService(retrofit: Retrofit): LikeService =
        retrofit.create(LikeService::class.java)

    @Provides
    @Singleton
    fun provideLikeRemoteDataSource(likeService: LikeService): LikeDataSource =
        LikeRemoteDataSource(likeService)

    @Provides
    @Singleton
    fun provideLikeRepository(likeRemoteDataSource: LikeDataSource): LikeRepository =
        LikeRepositoryImpl(likeRemoteDataSource)
}
