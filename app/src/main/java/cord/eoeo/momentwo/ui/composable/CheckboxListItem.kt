package cord.eoeo.momentwo.ui.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    Row(
        modifier = Modifier
            .height(60.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "",
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .padding(8.dp),
        )
        Text(
            text = itemText(),
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .weight(1f),
        )
        Checkbox(
            checked = isChecked(),
            onCheckedChange = onCheckedChange,
            modifier = Modifier.fillMaxHeight(),
        )
    }
}
