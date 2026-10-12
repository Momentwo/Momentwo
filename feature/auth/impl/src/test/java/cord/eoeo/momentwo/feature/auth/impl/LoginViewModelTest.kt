package cord.eoeo.momentwo.feature.auth.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.domain.login.RequestLoginUseCase
import cord.eoeo.momentwo.core.domain.login.TryAutoLoginUseCase
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.auth.impl.login.LoginContract
import cord.eoeo.momentwo.feature.auth.impl.login.LoginViewModel
import java.io.IOException
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
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
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
        vm.setEvent(LoginContract.Event.OnEmailEntered("fixture@example.com"))
        vm.setEvent(LoginContract.Event.OnPasswordEntered("FixtureOnly1!"))
        advanceUntilIdle()
        vm.setEvent(LoginContract.Event.OnLoginClicked)
        advanceUntilIdle()
        assertEquals(listOf("fixture@example.com" to "FixtureOnly1!"), login.requests)
        assertEquals("fixture-access", preferences.accessToken)
        assertEquals("fixture-refresh", preferences.refreshToken)
        assertEquals(fixtureProfile, profile.storedProfile)
        assertTrue(vm.uiState.value.isSuccess)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(LoginContract.Effect.NavigateToAlbum, effect.await())
    }

    @Test
    fun loginFailureShowsErrorWithoutPersistingSession() = runTest(mainDispatcherRule.testDispatcher) {
        login.result = Result.failure(IOException("Fixture failure"))
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.setEvent(LoginContract.Event.OnLoginClicked)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isError)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isSuccess)
        assertEquals("", preferences.accessToken)
        assertNull(profile.storedProfile)
        assertEquals(LoginContract.Effect.ShowSnackbar("로그인 요청에 실패했습니다"), effect.await())
    }

    @Test
    fun existingTokenAutoLoginNavigatesWithoutRequestingCredentials() = runTest(mainDispatcherRule.testDispatcher) {
        preferences.accessToken = "fixture-access"
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.setEvent(LoginContract.Event.OnRequestAutoLogin)
        advanceUntilIdle()
        assertEquals(LoginContract.Effect.NavigateToAlbum, effect.await())
        assertTrue(vm.uiState.value.isSuccess)
        assertTrue(login.requests.isEmpty())
    }

    @Test
    fun signUpClickProducesNavigationEffect() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.setEvent(LoginContract.Event.OnSignUpClicked)
        assertEquals(LoginContract.Effect.NavigateToSignUp, effect.await())
        assertTrue(login.requests.isEmpty())
    }
}
