package cord.eoeo.momentwo.data

object MomentwoApi {
    const val BASE_URL = "http://125.242.231.153:8080"

    // Sign Up
    const val POST_SIGN_UP = "/signup"
    const val POST_CHECK_EMAIL = "/check_email"
    const val POST_CHECK_NICKNAME = "/check_nickname"

    // Login
    const val POST_LOGIN = "/signin"

    // Friend
    const val POST_FRIEND_REQUEST = "/friendship/request"
    const val DELETE_FRIEND_REQUEST = "/friendship/request/cancel"
    const val POST_RESPONSE_FRIEND_REQUEST = "/friendship/response"
    const val GET_FRIEND_LIST = "/friendship"
    const val GET_SENT_FRIEND_REQUESTS = "/friendship/send"
    const val GET_RECEIVED_FRIEND_REQUESTS = "/friendship/receive"
}
