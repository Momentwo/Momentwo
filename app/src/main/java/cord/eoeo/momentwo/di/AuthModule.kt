package cord.eoeo.momentwo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cord.eoeo.momentwo.data.authentication.AuthAuthenticator
import cord.eoeo.momentwo.data.authentication.AuthInterceptor
import cord.eoeo.momentwo.data.authentication.PreferenceRepository
import cord.eoeo.momentwo.data.authentication.PreferenceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore("momentwo")

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userDataStore

    @Provides
    @Singleton
    fun providePreferenceRepository(userDataStore: DataStore<Preferences>): PreferenceRepository = PreferenceRepositoryImpl(userDataStore)

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferenceRepository: PreferenceRepository): AuthInterceptor = AuthInterceptor(preferenceRepository)

    @Provides
    @Singleton
    fun provideAuthAuthenticator(preferenceRepository: PreferenceRepository): AuthAuthenticator = AuthAuthenticator(preferenceRepository)
}
