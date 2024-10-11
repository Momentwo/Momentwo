package cord.eoeo.momentwo.ui.albumdetail.subalbumlist

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
import cord.eoeo.momentwo.ui.model.SubAlbumItem

@Composable
fun SubAlbumItemCard(
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

    SubAlbumItemCardScreen(
        imageLoader = imageLoader,
        subAlbumItem = subAlbumItem,
        isSelected = { isSelected },
        cardColors = { cardColors },
        isEditMode = isEditMode,
        onClick = onClick,
        onSelectedChange = { onChangeSubAlbumSelected(it, subAlbumItem().id) },
        modifier = modifier
    )
}

@Composable
fun SubAlbumItemCardScreen(
    imageLoader: ImageLoader,
    subAlbumItem: () -> SubAlbumItem,
    isSelected: () -> Boolean,
    cardColors: () -> CardColors,
    isEditMode: () -> Boolean,
    onClick: (Int, String) -> Unit,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            ElevatedCard(
                onClick = {
                    if (isEditMode().not()) {
                        onClick(subAlbumItem().id, subAlbumItem().title)
                    } else {
                        onSelectedChange(isSelected().not())
                    }
                },
                colors = cardColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                ) {
                    items(items = subAlbumItem().imageList, key = { it.id }) { item ->
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = "서브 앨범 대표 이미지",
                            imageLoader = imageLoader,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        )
                    }
                }
            }

            Text(
                text = subAlbumItem().title,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp, 0.dp, 0.dp),
            )
        }

        if (isEditMode()) {
            Checkbox(
                checked = isSelected(),
                onCheckedChange = onSelectedChange,
                colors = CheckboxDefaults.colors()
                    .copy(uncheckedBoxColor = Color.Gray, uncheckedBorderColor = Color.Gray),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}
