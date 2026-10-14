package cord.eoeo.momentwo.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun LibraryEditDialog(title: String, text: String, extra: String? = null, pending: Boolean, onText: (String) -> Unit, onExtra: (String) -> Unit = {}, onSave: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column {
            OutlinedTextField(value = text, onValueChange = onText, label = { Text("이름") }, singleLine = true)
            if (extra != null) OutlinedTextField(value = extra, onValueChange = onExtra, label = { Text("소제목") })
            if (pending) LinearProgressIndicator()
        }
    }, confirmButton = { TextButton(onClick = onSave, enabled = !pending && text.isNotBlank()) { Text("저장") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } })
}
