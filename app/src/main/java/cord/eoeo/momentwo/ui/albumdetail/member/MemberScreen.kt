package cord.eoeo.momentwo.ui.albumdetail.member

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import cord.eoeo.momentwo.ui.START_EFFECTS_KEY
import cord.eoeo.momentwo.ui.model.MemberItem

@Composable
fun MemberScreen(
    memberList: () -> List<MemberItem>,
    isEditMode: () -> Boolean,
    getMembers: () -> Unit,
    getFriends: () -> Unit,
    getIsSelected: (String) -> Boolean,
    onChangeMemberSelected: (Boolean, String) -> Unit,
    onBack: () -> Unit,
) {
    LifecycleStartEffect(START_EFFECTS_KEY) {
        getMembers()
        getFriends()
        onStopOrDispose { }
    }

    BackHandler(onBack = onBack)

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
