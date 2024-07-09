package cord.eoeo.momentwo.ui.album.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cord.eoeo.momentwo.ui.model.AlbumItem

@Composable
fun AlbumItemCard(
    albumItem: () -> AlbumItem,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        ElevatedCard(
            onClick = { /*TODO: Navigate to Album Detail*/ },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            /* TODO: 앨범 대표사진 Grid 구현 */
        }

        Text(
            text = albumItem().title,
            maxLines = 1,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 8.dp, 0.dp, 0.dp),
        )
    }
}
