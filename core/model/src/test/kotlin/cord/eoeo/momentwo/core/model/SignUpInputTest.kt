package cord.eoeo.momentwo.core.model

import org.junit.Assert.assertFalse
import org.junit.Test

class SignUpInputTest {
    @Test fun stringRepresentationDoesNotExposeAnyInputValue() {
        val input = SignUpInput("Private Name", "private@example.com", "PrivatePassword1!", "PrivateNick", "20000102", "01012345678")
        listOf(input.name,input.email,input.password,input.nickname,input.birthdayDigits,input.phoneDigits).forEach {
            assertFalse(input.toString().contains(it))
        }
    }
}
