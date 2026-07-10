package cord.eoeo.momentwo.core.data.di
import cord.eoeo.momentwo.core.common.dispatcher.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher

import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.data.friend.FriendDataSource
import cord.eoeo.momentwo.core.data.friend.FriendRemoteMediator
import cord.eoeo.momentwo.core.data.friend.FriendRepositoryImpl
import cord.eoeo.momentwo.core.database.FriendDao
import cord.eoeo.momentwo.core.data.friend.local.FriendLocalDataSource
import cord.eoeo.momentwo.core.database.FriendRemoteKeyDao
import cord.eoeo.momentwo.core.data.friend.remote.FriendRemoteDataSource
import cord.eoeo.momentwo.core.data.friend.remote.FriendService
import cord.eoeo.momentwo.core.data.friend.FriendRepository
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
    fun provideFriendRemoteDataSource(
        friendService: FriendService,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): FriendDataSource.Remote =
        FriendRemoteDataSource(friendService, dispatcher)

    @Provides
    @Singleton
    @QualifierModule.LocalDataSource
    fun provideFriendLocalDataSource(
        friendDao: FriendDao,
        friendRemoteKeyDao: FriendRemoteKeyDao,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): FriendDataSource.Local =
        FriendLocalDataSource(friendDao, friendRemoteKeyDao, dispatcher)

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
}
