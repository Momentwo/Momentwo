package cord.eoeo.momentwo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cord.eoeo.momentwo.data.datastore.PreferenceRepository
import cord.eoeo.momentwo.data.datastore.PreferenceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule {
    private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore("momentwo")

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userDataStore

    @Provides
    @Singleton
    fun providePreferenceRepository(userDataStore: DataStore<Preferences>): PreferenceRepository = PreferenceRepositoryImpl(userDataStore)
}
