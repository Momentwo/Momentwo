package cord.eoeo.momentwo.feature.auth.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.core.ui.CollectEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Composable
internal fun LoginScreen(
    coroutineScope: CoroutineScope,
    uiState: () -> LoginState,
    effectFlow: () -> Flow<LoginEffect>,
    onEvent: (event: LoginEvent) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            LoginEffect.NavigateToAlbum -> navigateToAlbum()
            LoginEffect.NavigateToSignUp -> navigateToSignUp()
            is LoginEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Momentwo",
            fontSize = 48.sp,
            fontFamily = FontFamily.Cursive,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .wrapContentHeight(align = Alignment.CenterVertically),
        )

        Column(
            modifier =
                Modifier
                    .weight(2f)
                    .fillMaxSize(),
        ) {
            TextField(
                value = uiState().email,
                singleLine = true,
                placeholder = { Text(text = "E-Mail") },
                leadingIcon = { Icon(Icons.Default.Email, "E-Mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { onEvent(LoginEvent.OnEmailEntered(it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 12.dp),
            )
            TextField(
                value = uiState().password,
                singleLine = true,
                placeholder = { Text(text = "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onEvent(LoginEvent.OnLoginClicked) }),
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { onEvent(LoginEvent.OnPasswordEntered(it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 12.dp),
            )

            Button(
                enabled = !uiState().isSubmittingCredentials,
                onClick = { onEvent(LoginEvent.OnLoginClicked) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 12.dp),
            ) {
                Text(text = "Login")
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "회원가입",
                    modifier =
                        Modifier
                            .padding(8.dp, 0.dp)
                            .clickable { onEvent(LoginEvent.OnSignUpClicked) },
                )
                Text(text = "|", modifier = Modifier.padding(16.dp, 0.dp))
                Text(
                    text = "아이디/비밀번호 찾기",
                    modifier =
                        Modifier
                            .padding(8.dp, 0.dp)
                            .clickable { /* TODO */ },
                )
            }
        }
    }
}
