package cord.eoeo.momentwo.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object QualifierModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RemoteDataSource

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class LocalDataSource
}
