package cord.eoeo.momentwo.core.data.library

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
abstract class LibraryModule {
    @Binds @Singleton abstract fun repository(impl: RoomLocalLibraryRepository): LocalLibraryRepository
}
