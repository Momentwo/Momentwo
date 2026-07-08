package cord.eoeo.momentwo.ui.photolist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.ui.model.PhotoItem
import cord.eoeo.momentwo.core.designsystem.theme.favoriteBorder
import cord.eoeo.momentwo.core.designsystem.theme.primaryDark

@Composable
fun PhotoListItemBox(
    imageLoader: ImageLoader,
    photoItem: () -> PhotoItem,
    isEditMode: () -> Boolean,
    isSelected: () -> Boolean,
    onChangeSelected: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable {
                if (isEditMode()) {
                    onChangeSelected(isSelected().not())
                } else {
                    onClick()
                }
            },
    ) {
        AsyncImage(
            model = photoItem().photoUrl,
            contentDescription = "사진",
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp)),
        )

        if (photoItem().isLiked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "",
                    tint = primaryDark,
                    modifier = Modifier.size(20.dp)
                )

                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "",
                    tint = favoriteBorder,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isEditMode()) {
            if (isSelected()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }

            Checkbox(
                checked = isSelected(),
                onCheckedChange = onChangeSelected,
                colors = CheckboxDefaults.colors()
                    .copy(uncheckedBoxColor = Color.Gray, uncheckedBorderColor = Color.Gray),
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}
