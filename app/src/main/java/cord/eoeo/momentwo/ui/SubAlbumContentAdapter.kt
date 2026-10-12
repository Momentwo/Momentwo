package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import coil.ImageLoader
import cord.eoeo.momentwo.ui.albumdetail.SubAlbumContentState
import cord.eoeo.momentwo.ui.albumdetail.SubAlbumContentActions
import cord.eoeo.momentwo.feature.subalbum.impl.SubAlbumListScreen

@Composable
internal fun SubAlbumContentAdapter(
    imageLoader: ImageLoader,
    state: SubAlbumContentState,
    actions: SubAlbumContentActions,
) {
    SubAlbumListScreen(
        imageLoader = imageLoader,
        subAlbumList = { state.items },
        isEditMode = { state.isEditMode },
        getSubAlbums = actions.getSubAlbums,
        getIsSelected = { state.selectedIds.contains(it) },
        onClickItem = actions.onClickItem,
        onChangeSubAlbumSelected = actions.onChangeSelected,
        onBack = actions.onBack,
    )
}
