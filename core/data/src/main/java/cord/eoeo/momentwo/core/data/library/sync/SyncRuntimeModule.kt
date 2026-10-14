package cord.eoeo.momentwo.core.data.library.sync
import cord.eoeo.momentwo.core.common.BackgroundTasksInitializer
import cord.eoeo.momentwo.core.data.library.SyncScheduling
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
internal object SyncRuntimeModule {
    @Provides @Singleton fun scheduling(impl:PlatformSyncScheduling):SyncScheduling=impl
    @Provides @Singleton fun bootstrap(impl:SyncBootstrap):BackgroundTasksInitializer=impl
    @Provides @Singleton fun engine(store:RoomSyncWorkStore)=SyncEngine(store)
}
