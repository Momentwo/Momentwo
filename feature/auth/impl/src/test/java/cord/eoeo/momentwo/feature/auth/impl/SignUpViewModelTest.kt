package cord.eoeo.momentwo.feature.auth.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpState
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpEvent
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpEffect
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule(kotlinx.coroutines.test.StandardTestDispatcher())
    private val repository = FakeSignUpRepository()
    private val store = ViewModelStore()
    private fun viewModel() = SignUpViewModel(repository).also { store.put("signup", it) }
    @After fun clearViewModel() { store.clear() }

    @Test
    fun emptyEmailStopsProgressAndShowsValidationMessage() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(SignUpEvent.OnNextClicked)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.email.isError)
        assertEquals("이메일 입력을 확인하세요", vm.uiState.value.email.errorMessage)
        assertEquals(SignUpEffect.ShowSnackbar("이메일 입력을 확인하세요"), effect.await())
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun validCredentialsAdvanceToSecondPage() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        vm.onEvent(SignUpEvent.OnEmailEntered("fixture@example.com"))
        vm.onEvent(SignUpEvent.OnPasswordEntered("FixtureOnly1!"))
        vm.onEvent(SignUpEvent.OnPasswordCheckEntered("FixtureOnly1!"))
        advanceUntilIdle()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(SignUpEvent.OnNextClicked)
        assertEquals(SignUpEffect.ScrollToNextPage, effect.await())
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun birthdayAndPhoneInputAreNormalizedAndBounded() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        vm.onEvent(SignUpEvent.OnBirthdayEntered("2000-01-02123"))
        vm.onEvent(SignUpEvent.OnPhoneEntered("010-1234-567899"))
        advanceUntilIdle()
        assertEquals("20000102", vm.uiState.value.birthday.value)
        assertEquals("01012345678", vm.uiState.value.phone.value)
    }

    @Test
    fun successfulSignUpFormatsRequestAndNavigatesToLogin() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        listOf(
            SignUpEvent.OnEmailEntered("fixture@example.com"),
            SignUpEvent.OnPasswordEntered("FixtureOnly1!"),
            SignUpEvent.OnNicknameEntered("fixture"),
            SignUpEvent.OnNameEntered("Fixture"),
            SignUpEvent.OnBirthdayEntered("20000102"),
            SignUpEvent.OnPhoneEntered("01012345678"),
        ).forEach(vm::onEvent)
        advanceUntilIdle()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(SignUpEvent.OnAcceptClicked)
        advanceUntilIdle()
        assertEquals("2000-01-02", repository.requests.single().birthday)
        assertEquals("010-1234-5678", repository.requests.single().phone)
        assertTrue(vm.uiState.value.isSuccess)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(SignUpEffect.NavigateToLogin, effect.await())
    }

    private fun fill(vm: SignUpViewModel) {
        listOf(
            SignUpEvent.OnEmailEntered("fixture@example.com"),
            SignUpEvent.OnPasswordEntered("FixtureOnly1!"),
            SignUpEvent.OnNicknameEntered("fixture"),
            SignUpEvent.OnNameEntered("Fixture"),
            SignUpEvent.OnBirthdayEntered("20000102"),
            SignUpEvent.OnPhoneEntered("01012345678"),
        ).forEach(vm::onEvent)
    }

    @Test fun editsDuringSubmissionArePreservedAndDuplicateIsIgnored() = runTest(mainDispatcherRule.testDispatcher) {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        repository.response = { response.await(); Result.success(Unit) }
        val vm = viewModel()
        fill(vm)
        vm.onEvent(SignUpEvent.OnAcceptClicked)
        runCurrent()
        vm.onEvent(SignUpEvent.OnNameEntered("Edited"))
        vm.onEvent(SignUpEvent.OnBirthdayEntered("19990101"))
        vm.onEvent(SignUpEvent.OnAcceptClicked)
        response.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, repository.requests.size)
        assertEquals("Fixture", repository.requests.single().name)
        assertEquals("2000-01-02", repository.requests.single().birthday)
        assertEquals("Edited", vm.uiState.value.name.value)
        assertEquals("19990101", vm.uiState.value.birthday.value)
        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test fun cancellationNeverBecomesSignUpError() = runTest(mainDispatcherRule.testDispatcher) {
        repository.result = Result.failure(kotlinx.coroutines.CancellationException("fixture cancellation"))
        val vm = viewModel()
        fill(vm)
        vm.onEvent(SignUpEvent.OnAcceptClicked)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isError)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun failureShowsNonBlankMessageAndKeepsInputs() = runTest(mainDispatcherRule.testDispatcher) {
        repository.result = Result.failure(java.io.IOException("offline"))
        val vm = viewModel()
        fill(vm)
        vm.onEvent(SignUpEvent.OnAcceptClicked)
        advanceUntilIdle()
        val effect = vm.effect.first() as SignUpEffect.ShowSnackbar
        assertTrue(effect.message.isNotBlank())
        assertEquals("fixture@example.com", vm.uiState.value.email.value)
        assertTrue(vm.uiState.value.isError)
    }
}
