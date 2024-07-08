package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.login.LoginDataSource
import cord.eoeo.momentwo.data.login.LoginRepository
import cord.eoeo.momentwo.data.login.LoginRepositoryImpl
import cord.eoeo.momentwo.data.login.remote.LoginRemoteDataSource
import cord.eoeo.momentwo.data.login.remote.LoginService
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
    fun provideLoginService(retrofit: Retrofit) =
        retrofit.create(LoginService::class.java)

    @Provides
    @Singleton
    fun provideLoginRemoteDataSource(loginService: LoginService): LoginDataSource =
        LoginRemoteDataSource(loginService)

    @Provides
    @Singleton
    fun provideLoginRepository(loginRemoteDataSource: LoginDataSource): LoginRepository =
        LoginRepositoryImpl(loginRemoteDataSource)
}
