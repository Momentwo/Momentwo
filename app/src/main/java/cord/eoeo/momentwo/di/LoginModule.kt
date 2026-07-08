package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.data.login.LoginDataSource
import cord.eoeo.momentwo.data.login.LoginRepositoryImpl
import cord.eoeo.momentwo.data.login.remote.LoginRemoteDataSource
import cord.eoeo.momentwo.data.login.remote.LoginService
import cord.eoeo.momentwo.domain.login.LoginRepository
import cord.eoeo.momentwo.domain.login.RequestLoginUseCase
import cord.eoeo.momentwo.domain.login.TryAutoLoginUseCase
import cord.eoeo.momentwo.domain.mapper.ProfileMapper
import cord.eoeo.momentwo.domain.profile.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    fun provideLoginService(retrofit: Retrofit) = retrofit.create(LoginService::class.java)

    @Provides
    @Singleton
    fun provideLoginRemoteDataSource(loginService: LoginService): LoginDataSource = LoginRemoteDataSource(loginService)

    @Provides
    @Singleton
    fun provideLoginRepository(
        loginRemoteDataSource: LoginDataSource,
        profileMapper: ProfileMapper,
    ): LoginRepository = LoginRepositoryImpl(loginRemoteDataSource, profileMapper)

    @Provides
    @Singleton
    fun provideRequestLoginUseCase(
        loginRepository: LoginRepository,
        profileRepository: ProfileRepository,
        preferenceRepository: PreferenceRepository,
    ): RequestLoginUseCase = RequestLoginUseCase(loginRepository, profileRepository, preferenceRepository)

    @Provides
    @Singleton
    fun provideTryAutoLoginUseCase(preferenceRepository: PreferenceRepository): TryAutoLoginUseCase =
        TryAutoLoginUseCase(preferenceRepository)
}
