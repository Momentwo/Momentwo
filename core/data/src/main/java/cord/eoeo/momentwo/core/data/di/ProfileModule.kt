package cord.eoeo.momentwo.core.data.di

import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.data.profile.ProfileDataSource
import cord.eoeo.momentwo.core.data.profile.ProfileRepositoryImpl
import cord.eoeo.momentwo.core.database.ProfileDao
import cord.eoeo.momentwo.core.data.profile.local.ProfileLocalDataSource
import cord.eoeo.momentwo.core.data.profile.remote.ProfileRemoteDataSource
import cord.eoeo.momentwo.core.data.profile.remote.ProfileService
import cord.eoeo.momentwo.core.data.mapper.ProfileMapper
import cord.eoeo.momentwo.core.data.profile.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {
    @Provides
    @Singleton
    fun provideProfileDao(database: MomentwoDatabase): ProfileDao = database.profileDao()

    @Provides
    @Singleton
    fun provideProfileService(retrofit: Retrofit) = retrofit.create(ProfileService::class.java)

    @Provides
    @Singleton
    @QualifierModule.LocalDataSource
    fun provideProfileLocalDataSource(profileDao: ProfileDao): ProfileDataSource.Local = ProfileLocalDataSource(profileDao)

    @Provides
    @Singleton
    @QualifierModule.RemoteDataSource
    fun provideProfileRemoteDataSource(profileService: ProfileService): ProfileDataSource.Remote = ProfileRemoteDataSource(profileService)

    @Provides
    @Singleton
    fun provideProfileRepository(
        @QualifierModule.LocalDataSource profileLocalDataSource: ProfileDataSource.Local,
        @QualifierModule.RemoteDataSource profileRemoteDataSource: ProfileDataSource.Remote,
        profileMapper: ProfileMapper,
    ): ProfileRepository = ProfileRepositoryImpl(profileLocalDataSource, profileRemoteDataSource, profileMapper)
}
