package cord.eoeo.momentwo.ui.albumdetail.member

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.model.MemberAuth
import cord.eoeo.momentwo.ui.model.MemberItem
import cord.eoeo.momentwo.ui.theme.starYellow

@Composable
fun MemberListItem(memberItem: () -> MemberItem) {
    Row(
        modifier =
            Modifier
                .height(60.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Box(modifier = Modifier.size(60.dp)) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
            )
            if (memberItem().auth == MemberAuth.ADMIN) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "",
                    tint = starYellow,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
        Text(
            text = memberItem().user.nickname,
            fontSize = 18.sp,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(start = 8.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = memberItem().auth.authString,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(end = 8.dp),
        )
    }
}