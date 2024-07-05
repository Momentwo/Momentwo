package cord.eoeo.momentwo.ui.signup

import cord.eoeo.momentwo.data.model.User
import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState

class SignUpContract {
    data class State(
        val email: InfoState = InfoState(),
        val password: InfoState = InfoState(),
        val passwordCheck: InfoState = InfoState(),
        val nickname: InfoState = InfoState(),
        val name: InfoState = InfoState(),
        val birthday: InfoState = InfoState(),
        val phone: InfoState = InfoState(),
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState {
        fun mapToUser(): User = User(
            name = name.value,
            username = email.value,
            password = password.value,
            nickname = nickname.value,
            birthday = "${birthday.value.substring(0..3)}-${birthday.value.substring(4..5)}-${birthday.value.substring(6)}",
            phone = "${phone.value.substring(0..2)}-${phone.value.substring(3..6)}-${phone.value.substring(7)}",
        )
    }

    data class InfoState(
        val value: String = "",
        val isError: Boolean = false,
        val errorMessage: String = "",
    )

    sealed interface Event : UiEvent {
        data class OnEmailEntered(val email: String) : Event
        data class OnPasswordEntered(val password: String) : Event
        data class OnPasswordCheckEntered(val passwordCheck: String) : Event
        data class OnNicknameEntered(val nickname: String) : Event
        data class OnNameEntered(val name: String) : Event
        data class OnBirthdayEntered(val birthday: String) : Event
        data class OnPhoneEntered(val phone: String) : Event
        data object OnNextClicked : Event
        data object OnAcceptClicked : Event
        data object OnBack : Event
        data class OnError(val errorMessage: String) : Event
    }

    sealed interface Effect : UiEffect {
        data object ScrollToNextPage : Effect
        data object ScrollToPreviousPage : Effect
        data object NavigateToLogin : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
