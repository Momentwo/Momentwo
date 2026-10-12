package cord.eoeo.momentwo.feature.albumdetail.impl.albumsetting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import coil.ImageLoader
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChangeImageRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    imageUri: () -> Uri?,
    title: () -> String,
    subTitle: () -> String,
    onChangeIsInChangeImage: (Boolean) -> Unit,
    onSelectImage: (Uri?) -> Unit,
    onBack: () -> Unit,
) {
    var isEdit: Boolean by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onSelectImage(it) }
    }

    ChangeImageScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        imageUri = imageUri,
        title = title,
        subTitle = subTitle,
        isEdit = { isEdit },
        bottomSheetState = { bottomSheetState },
        onChangeIsInChangeImage = onChangeIsInChangeImage,
        onChangeIsEdit = { isEdit = it },
        onClickSelect = { galleryLauncher.launch("image/*") },
        onClickReset = { onSelectImage(null) },
        onBack = onBack,
    )
}
