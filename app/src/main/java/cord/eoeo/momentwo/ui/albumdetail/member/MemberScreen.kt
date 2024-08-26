package cord.eoeo.momentwo.ui.albumdetail.member

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cord.eoeo.momentwo.ui.model.MemberAuth
import cord.eoeo.momentwo.ui.model.MemberItem
import cord.eoeo.momentwo.ui.model.UserItem

@Composable
fun MemberScreen() {
    val memberItems =
        listOf(
            MemberItem(UserItem(1, "User1"), MemberAuth.ADMIN),
            MemberItem(UserItem(2, "User2"), MemberAuth.SUB_ADMIN),
            MemberItem(UserItem(3, "User3"), MemberAuth.SUB_ADMIN),
            MemberItem(UserItem(4, "User4"), MemberAuth.MEMBER),
            MemberItem(UserItem(5, "User5"), MemberAuth.MEMBER),
            MemberItem(UserItem(6, "User6"), MemberAuth.MEMBER),
            MemberItem(UserItem(7, "User7"), MemberAuth.MEMBER),
            MemberItem(UserItem(8, "User8"), MemberAuth.MEMBER),
        )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = memberItems, key = { it.user.id }) { item ->
            MemberListItem(memberItem = { item })
        }
    }
}
