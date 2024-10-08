package cord.eoeo.momentwo.ui.albumdetail.albumsetting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import coil.ImageLoader

@Composable
fun ChangeImageRoute(
    imageLoader: ImageLoader,
    imageUrl: () -> String,
    onChangeIsInChangeImage: (Boolean) -> Unit,
    onClickChange: (Uri?) -> Unit,
    onBack: () -> Unit,
) {
    var imageUri: Uri? by rememberSaveable { mutableStateOf(Uri.parse(imageUrl())) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri ?: imageUri
    }

    ChangeImageScreen(
        imageLoader = imageLoader,
        imageUri = { imageUri },
        onChangeIsInChangeImage = onChangeIsInChangeImage,
        onClickSelect = { galleryLauncher.launch("image/*") },
        onClickChange = { onClickChange(imageUri) },
        onClickReset = { imageUri = null },
        onBack = onBack,
    )
}
