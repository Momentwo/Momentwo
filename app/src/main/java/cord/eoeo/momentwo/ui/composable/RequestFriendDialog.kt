package cord.eoeo.momentwo.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun RequestFriendDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nickname: String by remember { mutableStateOf("") }

    RequestFriendDialogScreen(
        nickname = { nickname },
        onNicknameChange = { nickname = it },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun RequestFriendDialogScreen(
    nickname: () -> String,
    onNicknameChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier =
                Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text(
                    text = "친구 요청을 보낼 유저의 별명을 입력하세요.",
                    fontWeight = FontWeight.SemiBold,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                )

                OutlinedTextField(
                    value = nickname(),
                    onValueChange = { onNicknameChange(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    TextButton(onClick = { onConfirm(nickname()) }) {
                        Text(text = "완료")
                    }
                }
            }
        }
    }
}
