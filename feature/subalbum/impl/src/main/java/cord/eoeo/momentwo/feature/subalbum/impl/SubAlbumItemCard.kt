package cord.eoeo.momentwo.feature.subalbum.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.core.model.SubAlbumItem

@Composable
internal fun SubAlbumItemCard(
    imageLoader: ImageLoader,
    subAlbumItem: () -> SubAlbumItem,
    isEditMode: () -> Boolean,
    getIsSelected: (Int) -> Boolean,
    onClick: (Int, String) -> Unit,
    onChangeSubAlbumSelected: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = getIsSelected(subAlbumItem().id)
    val cardColors = if (isSelected && isEditMode()) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    } else {
        CardDefaults.cardColors()
    }

    cord.eoeo.momentwo.core.ui.SubAlbumPreviewCard(
        imageLoader = imageLoader,
        title = subAlbumItem().title,
        images = subAlbumItem().imageList.map { cord.eoeo.momentwo.core.ui.SubAlbumPreview(it.id.toString(), it.imageUrl) },
        isSelected = { isSelected },
        cardColors = { cardColors },
        isEditMode = isEditMode,
        onClick = { onClick(subAlbumItem().id, subAlbumItem().title) },
        onSelectedChange = { onChangeSubAlbumSelected(it, subAlbumItem().id) },
        modifier = modifier
    )
}
