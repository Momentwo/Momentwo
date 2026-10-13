package cord.eoeo.momentwo.feature.albumdetail.impl.member

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cord.eoeo.momentwo.core.model.MemberItem

@Composable
internal fun MemberScreen(
    memberList: () -> List<MemberItem>,
    isEditMode: () -> Boolean,
    getMembers: () -> Unit,
    getFriends: () -> Unit,
    getIsSelected: (Int) -> Boolean,
    onChangeMemberSelected: (Boolean, Int) -> Unit,
    onBack: () -> Unit,
) {

    BackHandler(onBack = onBack)

    if (memberList().isEmpty()) { cord.eoeo.momentwo.core.ui.EmptyContent(); return }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = memberList(), key = { it.nickname }) { item ->
            MemberListItem(
                memberItem = { item },
                isEditMode = isEditMode,
                getIsSelected = getIsSelected,
                onChangeMemberSelected = onChangeMemberSelected,
            )
        }
    }
}
