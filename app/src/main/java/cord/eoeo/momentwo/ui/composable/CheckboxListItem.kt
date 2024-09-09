package cord.eoeo.momentwo.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CheckboxListItem(
    itemText: () -> String,
    onCheckedChange: (Boolean) -> Unit,
    isChecked: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    CheckboxListItemScreen(
        itemText = itemText,
        isChecked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
    )
}

@Composable
fun CheckboxListItemScreen(
    itemText: () -> String,
    isChecked: () -> Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = { Icon(Icons.Default.AccountCircle, "") },
        headlineContent = { Text(text = itemText()) },
        trailingContent = {
            Checkbox(
                checked = isChecked(),
                onCheckedChange = onCheckedChange,
            )
        },
    )
}
