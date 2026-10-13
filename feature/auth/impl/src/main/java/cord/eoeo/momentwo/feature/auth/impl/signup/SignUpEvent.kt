package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface SignUpEvent : UiEvent {
    data class OnEmailEntered(val email: String) : SignUpEvent
    data class OnPasswordEntered(val password: String) : SignUpEvent
    data class OnPasswordCheckEntered(val passwordCheck: String) : SignUpEvent
    data class OnNicknameEntered(val nickname: String) : SignUpEvent
    data class OnNameEntered(val name: String) : SignUpEvent
    data class OnBirthdayEntered(val birthday: String) : SignUpEvent
    data class OnPhoneEntered(val phone: String) : SignUpEvent
    data object OnNextClicked : SignUpEvent
    data object OnAcceptClicked : SignUpEvent
    data object OnBack : SignUpEvent
    data class OnError(val errorMessage: String) : SignUpEvent
}
