package cord.eoeo.momentwo.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun LibraryEditDialog(title: String, text: String, extra: String? = null, pending: Boolean, onText: (String) -> Unit, onExtra: (String) -> Unit = {}, onSave: () -> Unit, onDismiss: () -> Unit, textLabel: String = "이름", allowEmpty: Boolean = false) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column {
            OutlinedTextField(value = text, onValueChange = onText, label = { Text(textLabel) }, singleLine = textLabel == "이름")
            if (extra != null) OutlinedTextField(value = extra, onValueChange = onExtra, label = { Text("소제목") })
            if (pending) LinearProgressIndicator()
        }
    }, confirmButton = { TextButton(onClick = onSave, enabled = !pending && (allowEmpty || text.isNotBlank())) { Text("저장") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } })
}
