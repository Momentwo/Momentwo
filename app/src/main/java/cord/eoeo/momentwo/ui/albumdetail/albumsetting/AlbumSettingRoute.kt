package cord.eoeo.momentwo.ui.albumdetail.albumsetting

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cord.eoeo.momentwo.ui.model.MemberAuth

@Composable
fun AlbumSettingRoute(
    memberAuth: () -> MemberAuth,
    onOpenTextFieldDialog: (Int) -> Unit,
    onClickChangeImage: () -> Unit,
    onDeleteAlbum: () -> Unit,
    onExitAlbum: () -> Unit,
    onBack: () -> Unit,
) {
    val scrollableState = rememberScrollableState { 1f }
    val textColor = if (isSystemInDarkTheme()) Color.Gray else Color.DarkGray

    AlbumSettingScreen(
        scrollableState = scrollableState,
        memberAuth = memberAuth,
        textColor = { textColor },
        onOpenTextFieldDialog = onOpenTextFieldDialog,
        onClickChangeImage = onClickChangeImage,
        onDeleteAlbum = onDeleteAlbum,
        onExitAlbum = onExitAlbum,
        onBack = onBack,
    )
}
