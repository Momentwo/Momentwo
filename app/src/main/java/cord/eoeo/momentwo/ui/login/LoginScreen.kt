package cord.eoeo.momentwo.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope

@Composable
fun LoginScreen(
    coroutineScope: CoroutineScope,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
    ) { paddingValues ->
        Text(
            text = "Momentwo",
            fontSize = 48.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            textAlign = TextAlign.Center,
            lineHeight = 160.sp,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            TextField(
                value = "",
                placeholder = { Text(text = "E-Mail") },
                leadingIcon = { Icon(Icons.Default.Email, "E-Mail") },
                onValueChange = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 12.dp),
            )
            TextField(
                value = "",
                placeholder = { Text(text = "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                onValueChange = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 12.dp),
            )

            Button(
                onClick = { navigateToAlbum() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 12.dp),
            ) {
                Text(text = "Login")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "회원가입",
                    modifier = Modifier
                        .padding(8.dp, 0.dp)
                        .clickable { navigateToSignUp() },
                )
                Text(text = "|", modifier = Modifier.padding(16.dp, 0.dp))
                Text(
                    text = "아이디/비밀번호 찾기",
                    modifier = Modifier
                        .padding(8.dp, 0.dp)
                        .clickable { /* TODO */ },
                )
            }
        }
    }
}
