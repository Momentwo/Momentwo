package cord.eoeo.momentwo.core.network

import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.network.AuthAuthenticator
import cord.eoeo.momentwo.core.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(preferenceRepository: PreferenceRepository): AuthInterceptor = AuthInterceptor(preferenceRepository)

    @Provides
    @Singleton
    fun provideAuthAuthenticator(preferenceRepository: PreferenceRepository): AuthAuthenticator = AuthAuthenticator(preferenceRepository)
}
