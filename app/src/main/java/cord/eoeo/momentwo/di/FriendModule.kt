package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.friend.FriendDataSource
import cord.eoeo.momentwo.domain.friend.FriendRepository
import cord.eoeo.momentwo.data.friend.FriendRepositoryImpl
import cord.eoeo.momentwo.data.friend.remote.FriendRemoteDataSource
import cord.eoeo.momentwo.data.friend.remote.FriendService
import cord.eoeo.momentwo.domain.friend.GetFriendListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FriendModule {
    @Provides
    @Singleton
    fun provideFriendService(retrofit: Retrofit): FriendService = retrofit.create(FriendService::class.java)

    @Provides
    @Singleton
    fun provideFriendRemoteDataSource(friendService: FriendService): FriendDataSource = FriendRemoteDataSource(friendService)

    @Provides
    @Singleton
    fun provideFriendRepository(friendRemoteDataSource: FriendDataSource): FriendRepository = FriendRepositoryImpl(friendRemoteDataSource)

    @Provides
    @Singleton
    fun provideGetFriendListUseCase(friendRepository: FriendRepository) = GetFriendListUseCase(friendRepository)
}
