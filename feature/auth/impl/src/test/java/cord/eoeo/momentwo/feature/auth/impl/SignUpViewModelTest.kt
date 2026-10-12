package cord.eoeo.momentwo.feature.auth.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpContract
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val repository = FakeSignUpRepository()
    private val store = ViewModelStore()
    private fun viewModel() = SignUpViewModel(repository).also { store.put("signup", it) }
    @After fun clearViewModel() { store.clear() }

    @Test
    fun emptyEmailStopsProgressAndShowsValidationMessage() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.setEvent(SignUpContract.Event.OnNextClicked)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.email.isError)
        assertEquals("이메일 입력을 확인하세요", vm.uiState.value.email.errorMessage)
        assertEquals(SignUpContract.Effect.ShowSnackbar("이메일 입력을 확인하세요"), effect.await())
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun validCredentialsAdvanceToSecondPage() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        vm.setEvent(SignUpContract.Event.OnEmailEntered("fixture@example.com"))
        vm.setEvent(SignUpContract.Event.OnPasswordEntered("FixtureOnly1!"))
        vm.setEvent(SignUpContract.Event.OnPasswordCheckEntered("FixtureOnly1!"))
        advanceUntilIdle()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.setEvent(SignUpContract.Event.OnNextClicked)
        assertEquals(SignUpContract.Effect.ScrollToNextPage, effect.await())
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun birthdayAndPhoneInputAreNormalizedAndBounded() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        vm.setEvent(SignUpContract.Event.OnBirthdayEntered("2000-01-02123"))
        vm.setEvent(SignUpContract.Event.OnPhoneEntered("010-1234-567899"))
        advanceUntilIdle()
        assertEquals("20000102", vm.uiState.value.birthday.value)
        assertEquals("01012345678", vm.uiState.value.phone.value)
    }

    @Test
    fun successfulSignUpFormatsRequestAndNavigatesToLogin() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        listOf(
            SignUpContract.Event.OnEmailEntered("fixture@example.com"),
            SignUpContract.Event.OnPasswordEntered("FixtureOnly1!"),
            SignUpContract.Event.OnNicknameEntered("fixture"),
            SignUpContract.Event.OnNameEntered("Fixture"),
            SignUpContract.Event.OnBirthdayEntered("20000102"),
            SignUpContract.Event.OnPhoneEntered("01012345678"),
        ).forEach(vm::setEvent)
        advanceUntilIdle()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.setEvent(SignUpContract.Event.OnAcceptClicked)
        advanceUntilIdle()
        assertEquals("2000-01-02", repository.requests.single().birthday)
        assertEquals("010-1234-5678", repository.requests.single().phone)
        assertTrue(vm.uiState.value.isSuccess)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(SignUpContract.Effect.NavigateToLogin, effect.await())
    }
}
