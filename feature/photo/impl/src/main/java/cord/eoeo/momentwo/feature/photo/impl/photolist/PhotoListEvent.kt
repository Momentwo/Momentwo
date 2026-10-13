package cord.eoeo.momentwo.feature.photo.impl.photolist

import android.net.Uri
import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface PhotoListEvent : UiEvent {
    data class OnUploadImage(
        val imageUri: Uri,
    ) : PhotoListEvent

    data object OnRenameDraftChanged : PhotoListEvent
    data object OnPhotoVisited : PhotoListEvent

    data class OnChangeIsMenuExpended(
        val isMenuExpended: Boolean,
    ) : PhotoListEvent

    data class OnChangeIsEditMode(
        val isEditMode: Boolean,
    ) : PhotoListEvent

    data object OnClickCancelEdit : PhotoListEvent

    data object OnClickConfirmEdit : PhotoListEvent

    data class OnChangeIsSelected(
        val isSelected: Boolean,
        val photoId: Int,
    ) : PhotoListEvent

    data class OnChangeIsDialogOpened(
        val isDialogOpened: Boolean,
    ) : PhotoListEvent

    data class OnConfirmDialog(
        val subAlbumTitle: String,
    ) : PhotoListEvent

    data object OnRefreshPagingData : PhotoListEvent

    data object OnBack : PhotoListEvent

    data class OnError(
        val errorMessage: String,
    ) : PhotoListEvent
}
