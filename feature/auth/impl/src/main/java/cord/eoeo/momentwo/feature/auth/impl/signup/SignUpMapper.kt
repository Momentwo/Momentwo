package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.data.model.User

// DTO 경계 변경은 #157에서 수행한다. 이 전환은 기존 전송 형식을 보존한다.
internal fun SignUpState.mapToUser(): User = User(
    name = name.value,
    username = email.value,
    password = password.value,
    nickname = nickname.value,
    birthday = "${birthday.value.substring(0..3)}-${birthday.value.substring(4..5)}-${birthday.value.substring(6)}",
    phone = "${phone.value.substring(0..2)}-${phone.value.substring(3..6)}-${phone.value.substring(7)}",
)
