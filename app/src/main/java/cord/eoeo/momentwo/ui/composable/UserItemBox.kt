package cord.eoeo.momentwo.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.model.UserItem

@Composable
fun UserItemBox(
    userItem: () -> UserItem,
    onClickClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp),
    ) {
        IconButton(
            onClick = onClickClear,
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd),
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "",
                tint = Color.Gray,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Text(
                text = userItem().nickname,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )
        }
    }
}
