package cord.eoeo.momentwo.di

import android.content.Context
import androidx.room.Room
import cord.eoeo.momentwo.data.MomentwoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMomentwoDatabase(
        @ApplicationContext applicationContext: Context
    ): MomentwoDatabase = Room.databaseBuilder(
        applicationContext,
        MomentwoDatabase::class.java,
        "momentwo-database"
    ).build()
}
