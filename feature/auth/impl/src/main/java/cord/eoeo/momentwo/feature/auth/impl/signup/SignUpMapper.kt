package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.model.SignUpInput

internal fun SignUpState.toSignUpInput(): SignUpInput = SignUpInput(
    name = name.value,
    email = email.value,
    password = password.value,
    nickname = nickname.value,
    birthdayDigits = birthday.value,
    phoneDigits = phone.value,
)
