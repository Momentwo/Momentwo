package cord.eoeo.momentwo.ui.albumdetail.member

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cord.eoeo.momentwo.ui.model.MemberAuth
import cord.eoeo.momentwo.ui.model.MemberItem

@Composable
fun MemberScreen() {
    val memberItems =
        listOf(
            MemberItem("User1", MemberAuth.ADMIN),
            MemberItem("User2", MemberAuth.SUB_ADMIN),
            MemberItem("User3", MemberAuth.SUB_ADMIN),
            MemberItem("User4", MemberAuth.MEMBER),
            MemberItem("User5", MemberAuth.MEMBER),
            MemberItem("User6", MemberAuth.MEMBER),
            MemberItem("User7", MemberAuth.MEMBER),
            MemberItem("User8", MemberAuth.MEMBER),
        )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = memberItems, key = { it.nickname }) { item ->
            MemberListItem(memberItem = { item })
        }
    }
}
