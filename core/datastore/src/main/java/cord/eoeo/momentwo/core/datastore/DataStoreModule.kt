package cord.eoeo.momentwo.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore("momentwo")

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userDataStore

    @Provides
    @Singleton
    fun providePreferenceRepository(userDataStore: DataStore<Preferences>): PreferenceRepository =
        PreferenceRepositoryImpl(userDataStore)
}
