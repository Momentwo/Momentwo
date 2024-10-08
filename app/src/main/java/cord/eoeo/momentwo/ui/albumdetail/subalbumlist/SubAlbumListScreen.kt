package cord.eoeo.momentwo.ui.albumdetail.subalbumlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import coil.ImageLoader
import cord.eoeo.momentwo.ui.START_EFFECTS_KEY
import cord.eoeo.momentwo.ui.model.SubAlbumItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubAlbumListScreen(
    imageLoader: ImageLoader,
    subAlbumList: () -> List<SubAlbumItem>,
    isEditMode: () -> Boolean,
    getSubAlbums: () -> Unit,
    getIsSelected: (Int) -> Boolean,
    onClickItem: () -> Unit,
    onChangeSubAlbumSelected: (Boolean, Int) -> Unit,
    onBack: () -> Unit,
) {
    LifecycleStartEffect(START_EFFECTS_KEY) {
        getSubAlbums()
        onStopOrDispose { }
    }

    BackHandler(onBack = onBack)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 0.dp),
    ) {
        items(items = subAlbumList(), key = { it.id }) { item ->
            SubAlbumItemCard(
                imageLoader = imageLoader,
                subAlbumItem = { item },
                isEditMode = isEditMode,
                getIsSelected = getIsSelected,
                onClick = onClickItem,
                onChangeSubAlbumSelected = onChangeSubAlbumSelected,
                modifier = Modifier.animateItem()
            )
        }
    }
}
