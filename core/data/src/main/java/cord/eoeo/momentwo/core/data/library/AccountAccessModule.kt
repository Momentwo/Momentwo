package cord.eoeo.momentwo.core.data.library

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountAccessModule {
    @Binds abstract fun library(implementation: AccountLibraryRepository): AccountLibraryAccess
    @Binds abstract fun sync(implementation: AccountSyncRepository): AccountSyncOperations
}
