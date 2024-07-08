package cord.eoeo.momentwo.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    coroutineScope: CoroutineScope,
    uiState: () -> LoginContract.State,
    effectFlow: () -> Flow<LoginContract.Effect>,
    onEvent: (event: LoginContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow().onEach { effect ->
            when (effect) {
                is LoginContract.Effect.NavigateToAlbum -> {
                    navigateToAlbum()
                }

                is LoginContract.Effect.NavigateToSignUp -> {
                    navigateToSignUp()
                }

                is LoginContract.Effect.ShowSnackbar -> {
                    snackbarHostState().showSnackbar(
                        message = effect.message,
                    )
                }
            }
        }
    }

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
                value = uiState().email,
                singleLine = true,
                placeholder = { Text(text = "E-Mail") },
                leadingIcon = { Icon(Icons.Default.Email, "E-Mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { onEvent(LoginContract.Event.OnEmailEntered(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 12.dp),
            )
            TextField(
                value = uiState().password,
                singleLine = true,
                placeholder = { Text(text = "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onEvent(LoginContract.Event.OnLoginClicked) }),
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { onEvent(LoginContract.Event.OnPasswordEntered(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 12.dp),
            )

            Button(
                onClick = { onEvent(LoginContract.Event.OnLoginClicked) },
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
                        .clickable { onEvent(LoginContract.Event.OnSignUpClicked) },
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
