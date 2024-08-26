package cord.eoeo.momentwo.ui.albumdetail.subalbumlist

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
import cord.eoeo.momentwo.ui.model.SubAlbumItem

@Composable
fun SubAlbumItemCard(
    subAlbumItem: () -> SubAlbumItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        ElevatedCard(
            onClick = { /*TODO: Navigate to PhotoList*/ },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
        ) {
            // TODO: 서브 앨범 대표사진 Grid 구현, SubAlbumItem에 대표 사진 목록 필요 (최대 4개 사진), Coil ImageLoader 필요
        }

        Text(
            text = subAlbumItem().title,
            maxLines = 1,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp, 0.dp, 0.dp),
        )
    }
}
