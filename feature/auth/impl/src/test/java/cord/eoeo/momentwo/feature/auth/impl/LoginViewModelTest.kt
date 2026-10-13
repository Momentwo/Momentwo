package cord.eoeo.momentwo.feature.auth.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.domain.login.RequestLoginUseCase
import cord.eoeo.momentwo.core.domain.login.TryAutoLoginUseCase
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.auth.impl.login.LoginState
import cord.eoeo.momentwo.feature.auth.impl.login.LoginEvent
import cord.eoeo.momentwo.feature.auth.impl.login.LoginEffect
import cord.eoeo.momentwo.feature.auth.impl.login.LoginViewModel
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
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
class LoginViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())
    private val login = FakeLoginRepository()
    private val preferences = FakePreferenceRepository()
    private val profile = FakeProfileRepository()
    private val store = ViewModelStore()

    private fun viewModel() = LoginViewModel(
        RequestLoginUseCase(login, profile, preferences),
        TryAutoLoginUseCase(preferences),
    ).also { store.put("login", it) }

    @After fun clearViewModel() { store.clear() }

    @Test
    fun loginSuccessStoresSessionAndNavigatesToAlbum() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(LoginEvent.OnEmailEntered("fixture@example.com"))
        vm.onEvent(LoginEvent.OnPasswordEntered("FixtureOnly1!"))
        advanceUntilIdle()
        vm.onEvent(LoginEvent.OnLoginClicked)
        advanceUntilIdle()
        assertEquals(listOf("fixture@example.com" to "FixtureOnly1!"), login.requests)
        assertEquals("fixture-access", preferences.accessToken)
        assertEquals("fixture-refresh", preferences.refreshToken)
        assertEquals(fixtureProfile, profile.storedProfile)
        assertTrue(vm.uiState.value.isSuccess)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(LoginEffect.NavigateToAlbum, effect.await())
    }

    @Test
    fun loginFailureShowsErrorWithoutPersistingSession() = runTest(mainDispatcherRule.testDispatcher) {
        login.result = Result.failure(IOException("Fixture failure"))
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(LoginEvent.OnLoginClicked)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isError)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isSuccess)
        assertEquals("", preferences.accessToken)
        assertNull(profile.storedProfile)
        assertEquals(LoginEffect.ShowSnackbar("로그인 요청에 실패했습니다"), effect.await())
    }

    @Test
    fun existingTokenAutoLoginNavigatesWithoutRequestingCredentials() = runTest(mainDispatcherRule.testDispatcher) {
        preferences.accessToken = "fixture-access"
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(LoginEvent.OnRequestAutoLogin)
        advanceUntilIdle()
        assertEquals(LoginEffect.NavigateToAlbum, effect.await())
        assertTrue(vm.uiState.value.isSuccess)
        assertTrue(login.requests.isEmpty())
    }

    @Test
    fun signUpClickProducesNavigationEffect() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onEvent(LoginEvent.OnSignUpClicked)
        assertEquals(LoginEffect.NavigateToSignUp, effect.await())
        assertTrue(login.requests.isEmpty())
    }

    @Test fun inputChangesDuringLoginArePreservedAndDuplicateIsSkipped() = runTest(mainDispatcherRule.testDispatcher) {
        val response = CompletableDeferred<cord.eoeo.momentwo.core.model.LoginData>()
        login.response = { Result.success(response.await()) }
        val vm = viewModel()
        vm.onEvent(LoginEvent.OnEmailEntered("first@example.com"))
        vm.onEvent(LoginEvent.OnPasswordEntered("First1!"))
        vm.onEvent(LoginEvent.OnLoginClicked)
        runCurrent()
        vm.onEvent(LoginEvent.OnEmailEntered("edited@example.com"))
        vm.onEvent(LoginEvent.OnPasswordEntered("Edited1!"))
        vm.onEvent(LoginEvent.OnLoginClicked)
        response.complete(login.result.getOrThrow())
        advanceUntilIdle()
        assertEquals(listOf("first@example.com" to "First1!"), login.requests)
        assertEquals("edited@example.com", vm.uiState.value.email)
        assertEquals("Edited1!", vm.uiState.value.password)
        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test fun clearingViewModelDuringLoginCannotStoreSessionOrProduceLateResult() = runTest(mainDispatcherRule.testDispatcher) {
        val response = CompletableDeferred<cord.eoeo.momentwo.core.model.LoginData>()
        login.response = { Result.success(response.await()) }
        val vm = viewModel()
        vm.onEvent(LoginEvent.OnLoginClicked)
        runCurrent()
        val beforeClear = vm.uiState.value
        store.clear()
        response.complete(login.result.getOrThrow())
        advanceUntilIdle()
        assertEquals(beforeClear, vm.uiState.value)
        assertEquals("", preferences.accessToken)
        assertNull(profile.storedProfile)
    }

    @Test fun cancelledDependencyDoesNotBecomeLoginFailure() = runTest(mainDispatcherRule.testDispatcher) {
        login.result = Result.failure(CancellationException("cancelled dependency"))
        val vm = viewModel()
        vm.onEvent(LoginEvent.OnLoginClicked)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isError)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("", preferences.accessToken)
    }

    @Test fun initializationIsIdempotent() = runTest(mainDispatcherRule.testDispatcher) {
        preferences.accessToken = "fixture-access"
        val vm = viewModel()
        vm.initialize()
        advanceUntilIdle()
        vm.initialize()
        advanceUntilIdle()
        assertEquals(LoginEffect.NavigateToAlbum, vm.effect.first())
        assertEquals(1, preferences.accessReads)
        assertTrue(login.requests.isEmpty())
    }

    @Test fun explicitLoginReplacesPendingAutoLogin() = runTest(mainDispatcherRule.testDispatcher) {
        val autoToken = CompletableDeferred<String>()
        preferences.accessResponse = { Result.success(autoToken.await()) }
        val vm = viewModel()
        vm.initialize()
        runCurrent()
        vm.onEvent(LoginEvent.OnLoginClicked)
        runCurrent()
        autoToken.complete("old-auto-token")
        advanceUntilIdle()
        assertEquals(1, login.requests.size)
        assertTrue(vm.uiState.value.isSuccess)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("fixture-access", preferences.accessToken)
        assertEquals(LoginEffect.NavigateToAlbum, vm.effect.first())
    }
}
