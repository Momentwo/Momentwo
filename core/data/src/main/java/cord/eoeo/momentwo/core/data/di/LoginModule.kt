package cord.eoeo.momentwo.core.data.di
import cord.eoeo.momentwo.core.common.dispatcher.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher

import cord.eoeo.momentwo.core.data.login.LoginDataSource
import cord.eoeo.momentwo.core.data.login.LoginRepositoryImpl
import cord.eoeo.momentwo.core.data.login.remote.LoginRemoteDataSource
import cord.eoeo.momentwo.core.data.login.remote.LoginService
import cord.eoeo.momentwo.core.data.login.LoginRepository
import cord.eoeo.momentwo.core.data.mapper.ProfileMapper
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
    fun provideLoginRemoteDataSource(
        loginService: LoginService,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): LoginDataSource = LoginRemoteDataSource(loginService, dispatcher)

    @Provides
    @Singleton
    fun provideLoginRepository(
        loginRemoteDataSource: LoginDataSource,
        profileMapper: ProfileMapper,
    ): LoginRepository = LoginRepositoryImpl(loginRemoteDataSource, profileMapper)
}
