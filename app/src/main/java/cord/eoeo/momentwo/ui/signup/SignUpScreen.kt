package cord.eoeo.momentwo.ui.signup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
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
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow().onEach { effect ->
            when (effect) {
                SignUpContract.Effect.NavigateToLogin -> {
                    navigateToLogin()
                }
            }
        }.collect()
    }

    Scaffold(modifier = Modifier.fillMaxWidth()) { paddingValues ->
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
                    nickname = { uiState().nickname },
                ) { event -> onEvent(event) }
            } else {
                SecondSignUpPage(
                    name = { uiState().name },
                    birthday = { uiState().birthday },
                    phone1 = { uiState().phone1 },
                    phone2 = { uiState().phone2 },
                    phone3 = { uiState().phone3 },
                    address1 = { uiState().address1 },
                    address2 = { uiState().address2 },
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
                        coroutineScope.launch { pagerState().animateScrollToPage(page = 1) }
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
    email: () -> String,
    password: () -> String,
    passwordCheck: () -> String,
    nickname: () -> String,
    onEvent: (event: SignUpContract.Event) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp, 32.dp),
    ) {
        ItemText(text = { "E-Mail" })
        ItemTextField(
            value = email,
            placeholder = { "ex: momentwo@email.com" },
            keyboardType = { KeyboardType.Email },
        ) {
            onEvent(SignUpContract.Event.OnEmailEntered(it))
        }

        ItemText(text = { "비밀번호" })
        ItemTextField(
            value = password,
            placeholder = { "영문, 숫자, 특수문자 8~16자" },
            keyboardType = { KeyboardType.Password },
            visualTransformation = { PasswordVisualTransformation() },
        ) {
            onEvent(SignUpContract.Event.OnPasswordEntered(it))
        }

        ItemText(text = { "비밀번호 재입력" })
        ItemTextField(
            value = passwordCheck,
            placeholder = { "비밀번호를 다시 입력하세요" },
            keyboardType = { KeyboardType.Password },
            visualTransformation = { PasswordVisualTransformation() },
        ) {
            onEvent(SignUpContract.Event.OnPasswordCheckEntered(it))
        }

        ItemText(text = { "별명" })
        ItemTextField(value = nickname, placeholder = { "다른 사용자에게 보여질 별명" }) {
            onEvent(SignUpContract.Event.OnNicknameEntered(it))
        }
    }
}

@Composable
fun SecondSignUpPage(
    name: () -> String,
    birthday: () -> String,
    phone1: () -> String,
    phone2: () -> String,
    phone3: () -> String,
    address1: () -> String,
    address2: () -> String,
    onEvent: (event: SignUpContract.Event) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp, 32.dp),
    ) {
        ItemText(text = { "이름" })
        ItemTextField(value = name, placeholder = { "이름을 입력하세요" }) {
            onEvent(SignUpContract.Event.OnNameEntered(it))
        }

        ItemText(text = { "생년월일" })
        ItemTextField(value = birthday, placeholder = { "YYYY-MM-DD" }, keyboardType = { KeyboardType.Number }) {
            onEvent(SignUpContract.Event.OnBirthdayEntered(it))
        }

        ItemText(text = { "전화번호" })
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(0.dp, 8.dp, 0.dp, 24.dp),
        ) {
            PhoneNumberTextField(
                value = phone1,
                modifier = Modifier.weight(3f),
            ) { onEvent(SignUpContract.Event.OnPhone1Entered(it)) }
            PhoneNumberBorderText(
                modifier = Modifier
                    .height(TextFieldDefaults.MinHeight)
                    .weight(1f),
            )
            PhoneNumberTextField(
                value = phone2,
                modifier = Modifier.weight(4f),
            ) { onEvent(SignUpContract.Event.OnPhone2Entered(it)) }
            PhoneNumberBorderText(
                modifier = Modifier
                    .height(TextFieldDefaults.MinHeight)
                    .weight(1f),
            )
            PhoneNumberTextField(
                value = phone3,
                modifier = Modifier.weight(4f),
            ) { onEvent(SignUpContract.Event.OnPhone3Entered(it)) }
        }

        ItemText(text = { "주소" })
        ItemTextField(value = address1, placeholder = { "기본 주소" }, bottomPadding = { 0.dp }) {
            onEvent(SignUpContract.Event.OnAddress1Entered(it))
        }
        ItemTextField(value = address2, placeholder = { "상세 주소" }) {
            onEvent(SignUpContract.Event.OnAddress2Entered(it))
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
    bottomPadding: () -> Dp = { 24.dp },
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value(),
        onValueChange = { onValueChange(it) },
        singleLine = true,
        placeholder = { Text(text = placeholder()) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType()),
        visualTransformation = visualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp, 0.dp, bottomPadding()),
    )
}

@Composable
fun PhoneNumberTextField(
    value: () -> String,
    modifier: Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value(),
        onValueChange = { onValueChange(it) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier,
    )
}

@Composable
fun PhoneNumberBorderText(modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Text(
            text = "-",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
