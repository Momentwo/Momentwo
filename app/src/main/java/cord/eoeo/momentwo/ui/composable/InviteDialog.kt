package cord.eoeo.momentwo.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cord.eoeo.momentwo.ui.model.FriendItem
import cord.eoeo.momentwo.ui.model.UserItem

@Composable
fun InviteDialog(
    friendItemList: () -> List<FriendItem>,
    selectedFriendList: () -> List<FriendItem>,
    getIsChecked: (FriendItem) -> Boolean,
    onCheckedChange: (Boolean, FriendItem) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var searchQuery: String by rememberSaveable { mutableStateOf("") }
    val searchRegex = Regex(searchQuery, RegexOption.IGNORE_CASE)

    InviteDialogScreen(
        searchQuery = { searchQuery },
        searchRegex = { searchRegex },
        friendItemList = friendItemList,
        selectedFriendList = selectedFriendList,
        getIsChecked = getIsChecked,
        onQueryChange = { searchQuery = it },
        onSearch = { searchQuery = it },
        onCheckedChange = onCheckedChange,
        onDismiss = onDismiss,
        onConfirm = { onConfirm() },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InviteDialogScreen(
    searchQuery: () -> String,
    searchRegex: () -> Regex,
    friendItemList: () -> List<FriendItem>,
    selectedFriendList: () -> List<FriendItem>,
    getIsChecked: (FriendItem) -> Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onCheckedChange: (Boolean, FriendItem) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
        DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery(),
                        onQueryChange = onQueryChange,
                        onSearch = onSearch,
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(text = "친구 검색") },
                        leadingIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
                            }
                        },
                        trailingIcon = {
                            if (searchQuery().isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, "")
                                }
                            }
                        },
                    )
                },
                expanded = true,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedFriendList().isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth(),
                    ) {
                        items(items = selectedFriendList(), key = { it.nickname }) { friendItem ->
                            UserItemBox(
                                userItem = { UserItem(0, friendItem.nickname) },
                                onClickClear = { onCheckedChange(false, friendItem) },
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxHeight(),
                            )
                        }
                    }

                    HorizontalDivider()
                }

                LazyColumn(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(
                        items = friendItemList().filter { it.nickname.contains(searchRegex()) },
                        key = { it.nickname },
                    ) { friendItem ->
                        CheckboxListItem(
                            itemText = { friendItem.nickname },
                            onCheckedChange = { onCheckedChange(it, friendItem) },
                            isChecked = { getIsChecked(friendItem) },
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    TextButton(onClick = onConfirm) {
                        Text(text = "완료")
                    }
                }
            }
        }
    }
}
