package cord.eoeo.momentwo.di

import cord.eoeo.momentwo.data.signup.remote.SignUpService
import cord.eoeo.momentwo.data.signup.SignUpDataSource
import cord.eoeo.momentwo.data.signup.SignUpRepository
import cord.eoeo.momentwo.data.signup.SignUpRepositoryImpl
import cord.eoeo.momentwo.data.signup.remote.SignUpRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SignUpModule {
    @Provides
    @Singleton
    fun provideSignUpService(retrofit: Retrofit) =
        retrofit.create(SignUpService::class.java)

    @Provides
    @Singleton
    fun provideSignUpRemoteDataSource(signUpService: SignUpService): SignUpDataSource =
        SignUpRemoteDataSource(signUpService)

    @Provides
    @Singleton
    fun provideSignUpRepository(signUpRemoteDataSource: SignUpDataSource): SignUpRepository =
        SignUpRepositoryImpl(signUpRemoteDataSource)
}
