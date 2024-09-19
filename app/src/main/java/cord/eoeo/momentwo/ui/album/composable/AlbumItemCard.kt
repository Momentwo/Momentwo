package cord.eoeo.momentwo.ui.album.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.ui.model.AlbumItem

@Composable
fun AlbumItemCard(
    imageLoader: ImageLoader,
    albumItem: () -> AlbumItem,
    navigateToAlbumDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        ElevatedCard(
            onClick = navigateToAlbumDetail,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = albumItem().imageUrl,
                contentDescription = "대표 이미지",
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp)),
            )
            Text(
                text = albumItem().title,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            )
            Text(
                text = albumItem().subTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            )
        }
    }
}
