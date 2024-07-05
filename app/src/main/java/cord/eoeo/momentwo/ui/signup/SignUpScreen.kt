package cord.eoeo.momentwo.ui.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.signup.transformation.DateVisualTransformation
import cord.eoeo.momentwo.ui.signup.transformation.PhoneVisualTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignUpScreen(
    coroutineScope: CoroutineScope,
    pagerState: () -> PagerState,
    uiState: () -> SignUpContract.State,
    effectFlow: () -> Flow<SignUpContract.Effect>,
    onEvent: (event: SignUpContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow().onEach { effect ->
            when (effect) {
                is SignUpContract.Effect.ScrollToNextPage -> {
                    launch { pagerState().animateScrollToPage(page = 1) }
                }

                is SignUpContract.Effect.ScrollToPreviousPage -> {
                    launch { pagerState().animateScrollToPage(page = 0) }
                }

                is SignUpContract.Effect.NavigateToLogin -> {
                    navigateToLogin()
                }

                is SignUpContract.Effect.ShowSnackbar -> {
                    snackbarHostState().showSnackbar(
                        message = effect.message,
                    )
                }
            }
        }.collect()
    }

    BackHandler {
        if (pagerState().currentPage == 0) {
            popBackStack()
        } else {
            onEvent(SignUpContract.Event.OnBack)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState()) },
        modifier = Modifier.fillMaxWidth()
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState(),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(),
            beyondBoundsPageCount = 1,
        ) { page ->
            if (page == 0) {
                FirstSignUpPage(
                    email = { uiState().email },
                    password = { uiState().password },
                    passwordCheck = { uiState().passwordCheck },
                ) { event -> onEvent(event) }
            } else {
                SecondSignUpPage(
                    nickname = { uiState().nickname },
                    name = { uiState().name },
                    birthday = { uiState().birthday },
                    phone = { uiState().phone },
                ) { event -> onEvent(event) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 0.dp, 32.dp, 32.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
        ) {
            Button(
                onClick = {
                    if (pagerState().currentPage == 0) {
                        onEvent(SignUpContract.Event.OnNextClicked)
                    } else {
                        onEvent(SignUpContract.Event.OnAcceptClicked)
                    }
                },
            ) {
                if (pagerState().currentPage == 0) {
                    Text(text = "다음")
                } else {
                    Text(text = "확인")
                }
            }
        }
    }
}

@Composable
fun FirstSignUpPage(
    email: () -> SignUpContract.InfoState,
    password: () -> SignUpContract.InfoState,
    passwordCheck: () -> SignUpContract.InfoState,
    onEvent: (event: SignUpContract.Event) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp, 32.dp),
    ) {
        ItemText(text = { "E-Mail" })
        ItemTextField(
            value = { email().value },
            placeholder = { "ex: momentwo@email.com" },
            keyboardType = { KeyboardType.Email },
            isError = { email().isError },
            errorMessage = { email().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnEmailEntered(it))
        }

        ItemText(text = { "비밀번호" })
        ItemTextField(
            value = { password().value },
            placeholder = { "영문, 숫자, 특수문자 8~20자" },
            keyboardType = { KeyboardType.Password },
            visualTransformation = { PasswordVisualTransformation() },
            isError = { password().isError },
            errorMessage = { password().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnPasswordEntered(it))
        }

        ItemText(text = { "비밀번호 재입력" })
        ItemTextField(
            value = { passwordCheck().value },
            placeholder = { "비밀번호를 다시 입력하세요" },
            keyboardType = { KeyboardType.Password },
            visualTransformation = { PasswordVisualTransformation() },
            isError = { passwordCheck().isError },
            errorMessage = { passwordCheck().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnPasswordCheckEntered(it))
        }
    }
}

@Composable
fun SecondSignUpPage(
    nickname: () -> SignUpContract.InfoState,
    name: () -> SignUpContract.InfoState,
    birthday: () -> SignUpContract.InfoState,
    phone: () -> SignUpContract.InfoState,
    onEvent: (event: SignUpContract.Event) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp, 32.dp),
    ) {
        ItemText(text = { "별명" })
        ItemTextField(
            value = { nickname().value },
            placeholder = { "다른 사용자에게 보여질 별명" },
            isError = { nickname().isError },
            errorMessage = { nickname().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnNicknameEntered(it))
        }

        ItemText(text = { "이름" })
        ItemTextField(
            value = { name().value },
            placeholder = { "이름을 입력하세요" },
            isError = { name().isError },
            errorMessage = { name().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnNameEntered(it))
        }

        ItemText(text = { "생년월일" })
        ItemTextField(
            value = { birthday().value },
            placeholder = { "ex: 2024-01-01" },
            keyboardType = { KeyboardType.Number },
            visualTransformation = { DateVisualTransformation() },
            isError = { birthday().isError },
            errorMessage = { birthday().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnBirthdayEntered(it))
        }

        ItemText(text = { "전화번호" })
        ItemTextField(
            value = { phone().value },
            placeholder = { "번호만 입력하세요" },
            keyboardType = { KeyboardType.Phone },
            visualTransformation = { PhoneVisualTransformation() },
            isError = { phone().isError },
            errorMessage = { phone().errorMessage },
        ) {
            onEvent(SignUpContract.Event.OnPhoneEntered(it))
        }
    }
}

@Composable
fun ItemText(
    text: () -> String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier,
    )
}

@Composable
fun ItemTextField(
    value: () -> String,
    placeholder: () -> String,
    keyboardType: () -> KeyboardType = { KeyboardType.Text },
    visualTransformation: () -> VisualTransformation = { VisualTransformation.None },
    isError: () -> Boolean,
    errorMessage: () -> String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value(),
        onValueChange = { onValueChange(it) },
        singleLine = true,
        placeholder = { Text(text = placeholder()) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType()),
        visualTransformation = visualTransformation(),
        isError = isError(),
        colors = OutlinedTextFieldDefaults.colors(
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorTextColor = MaterialTheme.colorScheme.error,
        ),
        supportingText = { if (isError()) Text(text = errorMessage()) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp),
    )
}
