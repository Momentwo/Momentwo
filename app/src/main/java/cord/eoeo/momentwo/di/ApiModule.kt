package cord.eoeo.momentwo.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cord.eoeo.momentwo.data.MomentwoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideJsonParser(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(jsonParser: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(MomentwoApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(jsonParser))
            .build()
}
