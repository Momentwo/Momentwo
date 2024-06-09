package cord.eoeo.momentwo.ui.signup

import cord.eoeo.momentwo.data.model.User
import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState

class SignUpContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val passwordCheck: String = "",
        val nickname: String = "",
        val name: String = "",
        val birthday: String = "",
        val phone1: String = "",
        val phone2: String = "",
        val phone3: String = "",
        val address1: String = "",
        val address2: String = "",
        val isLoading: Boolean = false,
        val isError: Boolean = false,
    ) : UiState {
        fun mapToUser(): User = User(
            name = name,
            username = email,
            password = password,
            nickname = nickname,
            birthday = birthday,
            phone = "$phone1-$phone2-$phone3",
            address = "$address1 $address2",
        )
    }

    sealed interface Event : UiEvent {
        data class OnEmailEntered(val email: String) : Event
        data class OnPasswordEntered(val password: String) : Event
        data class OnPasswordCheckEntered(val password: String) : Event
        data class OnNicknameEntered(val nickname: String) : Event
        data class OnNameEntered(val name: String) : Event
        data class OnBirthdayEntered(val birthday: String) : Event
        data class OnPhone1Entered(val phone1: String) : Event
        data class OnPhone2Entered(val phone2: String) : Event
        data class OnPhone3Entered(val phone3: String) : Event
        data class OnAddress1Entered(val address1: String) : Event
        data class OnAddress2Entered(val address2: String) : Event
        data object OnAcceptClicked : Event
    }

    sealed interface Effect : UiEffect {
        data object NavigateToLogin : Effect
    }
}
