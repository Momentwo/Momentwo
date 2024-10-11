package cord.eoeo.momentwo.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cord.eoeo.momentwo.ui.theme.MomentwoTheme

@Composable
fun TextFieldDialog(
    titleText: () -> String,
    description: () -> String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    placeholder: () -> String = { "" },
) {
    var text: String by rememberSaveable { mutableStateOf("") }

    TextFieldDialogScreen(
        titleText = titleText,
        description = description,
        text = { text },
        placeholder = placeholder,
        onTextChange = { text = it },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
fun TextFieldDialogScreen(
    titleText: () -> String,
    description: () -> String,
    text: () -> String,
    placeholder: () -> String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = titleText(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )

                Text(
                    text = description(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )

                OutlinedTextField(
                    value = text(),
                    onValueChange = onTextChange,
                    singleLine = true,
                    placeholder = { Text(placeholder()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "취소", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { onConfirm(text()) }) {
                        Text(text = "확인", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TextFieldDialogPreview() {
    MomentwoTheme {
        TextFieldDialog(
            titleText = { "Title" },
            description = { "desc" },
            placeholder = { "init" },
            onDismiss = {},
            onConfirm = {},
        )
    }
}