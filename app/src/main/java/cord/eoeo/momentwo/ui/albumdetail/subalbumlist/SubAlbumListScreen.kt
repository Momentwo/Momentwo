package cord.eoeo.momentwo.ui.albumdetail.subalbumlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cord.eoeo.momentwo.ui.model.SubAlbumItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubAlbumListScreen() {
    val fakeItems =
        listOf(
            SubAlbumItem(1, "Sub1", emptyList()),
            SubAlbumItem(2, "Sub2", emptyList()),
            SubAlbumItem(3, "Sub3", emptyList()),
            SubAlbumItem(4, "Sub4", emptyList()),
            SubAlbumItem(5, "Sub5", emptyList()),
            SubAlbumItem(6, "Sub6", emptyList()),
            SubAlbumItem(7, "Sub7", emptyList()),
            SubAlbumItem(8, "Sub8", emptyList()),
            SubAlbumItem(9, "Sub9", emptyList()),
            SubAlbumItem(10, "Sub10", emptyList()),
            SubAlbumItem(11, "Sub11", emptyList()),
            SubAlbumItem(12, "Sub12", emptyList()),
            SubAlbumItem(13, "Sub13", emptyList()),
        )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp, 0.dp),
    ) {
        items(items = fakeItems, key = { it.id }) { item ->
            SubAlbumItemCard(
                subAlbumItem = { item },
                modifier = Modifier.animateItemPlacement(),
            )
        }
    }
}
