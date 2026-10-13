package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.common.UiError

internal sealed interface SignUpAction {
    data class InputChanged(val field: SignUpField, val value: String) : SignUpAction
    data class Validated(val errors: Map<SignUpField, String?>) : SignUpAction
    data object Started : SignUpAction
    data object Succeeded : SignUpAction
    data class Failed(val error: UiError) : SignUpAction
    data object Finished : SignUpAction
}
