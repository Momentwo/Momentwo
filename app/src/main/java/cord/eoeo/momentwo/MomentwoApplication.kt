package cord.eoeo.momentwo
import android.app.Application
import cord.eoeo.momentwo.core.common.BackgroundTasksInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
@HiltAndroidApp
class MomentwoApplication:Application(){
    @Inject lateinit var background:BackgroundTasksInitializer
    override fun onCreate(){super.onCreate();background.start()}
}
