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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cord.eoeo.momentwo.ui.model.UserItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InviteDialog(
    onDismissRequest: () -> Unit
) {
    val fakeFriendItems = listOf(
        UserItem(1, "User1"),
        UserItem(2, "User22"),
        UserItem(3, "User333"),
        UserItem(4, "User4"),
        UserItem(5, "User55"),
        UserItem(6, "User6666"),
        UserItem(7, "User77"),
        UserItem(8, "User8"),
        UserItem(9, "User9999"),
        UserItem(10, "User10"),
        UserItem(11, "User1111"),
        UserItem(12, "User12"),
    )

    var str: String by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 48.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            SearchBar(
                query = str,
                onQueryChange = { str = it },
                onSearch = {},
                active = true,
                onActiveChange = {},
                placeholder = { Text(text = "친구 검색") },
                leadingIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { /*TODO: Search*/ }) {
                        Icon(Icons.Default.Search, "")
                    }
                },
            ) {
                LazyRow(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth(),
                ) {
                    items(items = fakeFriendItems, key = { it.id }) { userItem ->
                        UserItemBox(
                            userItem = { userItem },
                            onClickClear = { /*TODO*/ },
                            modifier = Modifier
                                .animateItemPlacement()
                                .fillMaxHeight(),
                        )
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(items = fakeFriendItems, key = { it.id }) { userItem ->
                        ListItem(
                            leadingContent = { Icon(Icons.Default.AccountCircle, "") },
                            headlineContent = { Text(text = userItem.nickname) },
                            trailingContent = { Checkbox(checked = true, onCheckedChange = { /**/ }) },
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    TextButton(onClick = { /*TODO: Confirm Invite Dialog*/ }) {
                        Text(text = "완료")
                    }
                }
            }
        }
    }
}
