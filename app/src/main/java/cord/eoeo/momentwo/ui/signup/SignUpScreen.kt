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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignUpScreen(
    coroutineScope: CoroutineScope,
    pagerState: () -> PagerState,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = Modifier
            .fillMaxWidth(),
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState(),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(),
            beyondBoundsPageCount = 1,
        ) { page ->
            if (page == 0) {
                FirstSignUpPage()
            } else {
                SecondSignUpPage()
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
                        navigateToLogin()
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
fun FirstSignUpPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp, 32.dp),
    ) {
        ItemText(text = { "E-Mail" })
        ItemTextField(value = { "" }, placeholder = { "ex: momentwo@email.com" }) {
            /* TODO */
        }

        ItemText(text = { "비밀번호" })
        ItemTextField(value = { "" }, placeholder = { "영문, 숫자, 특수문자 8~16자" }) {
            /* TODO */
        }

        ItemText(text = { "비밀번호 재입력" })
        ItemTextField(value = { "" }, placeholder = { "비밀번호를 다시 입력하세요" }) {
            /* TODO */
        }

        ItemText(text = { "별명" })
        ItemTextField(value = { "" }, placeholder = { "다른 사용자에게 보여질 별명" }) {
            /* TODO */
        }
    }
}

@Composable
fun SecondSignUpPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp, 32.dp),
    ) {
        ItemText(text = { "이름" })
        ItemTextField(value = { "" }, placeholder = { "이름을 입력하세요" }) {
            /* TODO */
        }

        ItemText(text = { "생년월일" })
        ItemTextField(value = { "" }, placeholder = { "YYYY-MM-DD" }) {
            /* TODO */
        }

        ItemText(text = { "전화번호" })
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(0.dp, 8.dp, 0.dp, 24.dp),
        ) {
            PhoneNumberTextField(
                value = { "" },
                modifier = Modifier.weight(3f)
            ) {
                /* TODO */
            }
            PhoneNumberBorderText(modifier = Modifier
                .height(TextFieldDefaults.MinHeight)
                .weight(1f))
            PhoneNumberTextField(
                value = { "" },
                modifier = Modifier.weight(4f)
            ) {
                /* TODO */
            }
            PhoneNumberBorderText(modifier = Modifier
                .height(TextFieldDefaults.MinHeight)
                .weight(1f))
            PhoneNumberTextField(
                value = { "" },
                modifier = Modifier.weight(4f)
            ) {
                /* TODO */
            }
        }

        ItemText(text = { "주소" })
        ItemTextField(value = { "" }, placeholder = { "기본 주소" }, bottomPadding = { 0.dp }) {
            /* TODO */
        }
        ItemTextField(value = { "" }, placeholder = { "상세 주소" }) {
            /* TODO */
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
    bottomPadding: () -> Dp = { 24.dp },
    onValueChange: () -> Unit
) {
    TextField(
        value = value(),
        onValueChange = { onValueChange() },
        singleLine = true,
        placeholder = { Text(text = placeholder()) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp, 0.dp, bottomPadding()),
    )
}

@Composable
fun PhoneNumberTextField(
    value: () -> String,
    modifier: Modifier,
    onValueChange: () -> Unit
) {
    TextField(
        value = value(),
        onValueChange = { onValueChange() },
        singleLine = true,
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
