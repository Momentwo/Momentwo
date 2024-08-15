package cord.eoeo.momentwo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil.imageLoader
import cord.eoeo.momentwo.ui.theme.MomentwoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageLoader = applicationContext.imageLoader

        setContent {
            MomentwoTheme {
                MomentwoApp(imageLoader)
            }
        }
    }
}
