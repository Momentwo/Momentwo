package cord.eoeo.momentwo.ui.albumdetail.albumsetting

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.runtime.Composable
import cord.eoeo.momentwo.ui.model.MemberAuth

@Composable
fun AlbumSettingRoute(memberAuth: () -> MemberAuth) {
    val scrollableState = rememberScrollableState { 1f }

    AlbumSettingScreen(
        scrollableState = scrollableState,
        memberAuth = memberAuth,
    )
}
