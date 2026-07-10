package cord.eoeo.momentwo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import coil.imageLoader
import cord.eoeo.momentwo.core.designsystem.theme.MomentwoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageLoader = applicationContext.imageLoader
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MomentwoTheme {
                MomentwoApp(
                    imageLoader = imageLoader,
                    insetsController = insetsController,
                )
            }
        }
    }
}
