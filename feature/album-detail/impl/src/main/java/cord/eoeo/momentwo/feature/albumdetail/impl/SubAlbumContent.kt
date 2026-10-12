package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.model.SubAlbumItem

/** State slice supplied to the app-owned SubAlbum composition adapter. */
data class SubAlbumContentState(
    val items: List<SubAlbumItem>,
    val isEditMode: Boolean,
    val selectedIds: List<Int>,
)

class SubAlbumContentActions(
    val getSubAlbums: () -> Unit,
    val onClickItem: (Int, String) -> Unit,
    val onChangeSelected: (Boolean, Int) -> Unit,
    val onBack: () -> Unit,
)
