package cord.eoeo.momentwo.core.data.signup

import cord.eoeo.momentwo.core.data.model.User
import cord.eoeo.momentwo.core.model.SignUpInput

internal fun SignUpInput.toUserDto(): User {
    require(birthdayDigits.length == 8 && birthdayDigits.all { it in '0'..'9' }) {
        "Birthday must contain eight ASCII digits"
    }
    require(phoneDigits.length == 11 && phoneDigits.all { it in '0'..'9' }) {
        "Phone must contain eleven ASCII digits"
    }
    return User(
        name = name,
        username = email,
        password = password,
        nickname = nickname,
        birthday = "${birthdayDigits.substring(0, 4)}-${birthdayDigits.substring(4, 6)}-${birthdayDigits.substring(6)}",
        phone = "${phoneDigits.substring(0, 3)}-${phoneDigits.substring(3, 7)}-${phoneDigits.substring(7)}",
    )
}
