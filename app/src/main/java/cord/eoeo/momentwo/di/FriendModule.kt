package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.data.friend.FriendDataSource
import cord.eoeo.momentwo.data.friend.FriendRemoteMediator
import cord.eoeo.momentwo.data.friend.FriendRepositoryImpl
import cord.eoeo.momentwo.core.database.FriendDao
import cord.eoeo.momentwo.data.friend.local.FriendLocalDataSource
import cord.eoeo.momentwo.core.database.FriendRemoteKeyDao
import cord.eoeo.momentwo.data.friend.remote.FriendRemoteDataSource
import cord.eoeo.momentwo.data.friend.remote.FriendService
import cord.eoeo.momentwo.domain.friend.FriendRepository
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
    fun provideFriendDao(database: MomentwoDatabase): FriendDao = database.friendDao()

    @Provides
    @Singleton
    fun provideFriendRemoteKeyDao(database: MomentwoDatabase): FriendRemoteKeyDao = database.friendRemoteKeyDao()

    @Provides
    @Singleton
    fun provideFriendService(retrofit: Retrofit): FriendService = retrofit.create(FriendService::class.java)

    @Provides
    @Singleton
    @QualifierModule.RemoteDataSource
    fun provideFriendRemoteDataSource(friendService: FriendService): FriendDataSource.Remote =
        FriendRemoteDataSource(friendService)

    @Provides
    @Singleton
    @QualifierModule.LocalDataSource
    fun provideFriendLocalDataSource(
        friendDao: FriendDao,
        friendRemoteKeyDao: FriendRemoteKeyDao
    ): FriendDataSource.Local =
        FriendLocalDataSource(friendDao, friendRemoteKeyDao)

    @Provides
    @Singleton
    fun provideFriendRemoteMediator(
        @QualifierModule.RemoteDataSource friendRemoteDataSource: FriendDataSource.Remote,
        @QualifierModule.LocalDataSource friendLocalDataSource: FriendDataSource.Local,
    ): FriendRemoteMediator =
        FriendRemoteMediator(friendRemoteDataSource, friendLocalDataSource)

    @Provides
    @Singleton
    fun provideFriendRepository(
        @QualifierModule.RemoteDataSource friendRemoteDataSource: FriendDataSource.Remote,
        @QualifierModule.LocalDataSource friendLocalDataSource: FriendDataSource.Local,
        friendRemoteMediator: FriendRemoteMediator,
    ): FriendRepository =
        FriendRepositoryImpl(friendRemoteDataSource, friendLocalDataSource, friendRemoteMediator)

    @Provides
    @Singleton
    fun provideGetFriendListUseCase(friendRepository: FriendRepository) =
        GetFriendListUseCase(friendRepository)
}
