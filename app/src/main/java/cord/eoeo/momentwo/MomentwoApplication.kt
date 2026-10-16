package cord.eoeo.momentwo

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import cord.eoeo.momentwo.core.common.BackgroundTasksInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MomentwoApplication : Application(), SingletonImageLoader.Factory {
    @Inject lateinit var background: BackgroundTasksInitializer

    override fun onCreate() {
        super.onCreate()
        background.start()
    }

    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                // Preserve Coil 2's HTTP cache policy with an independent, unauthenticated client.
                add(OkHttpNetworkFetcherFactory(cacheStrategy = { CacheControlCacheStrategy() }))
            }
            .build()
}
