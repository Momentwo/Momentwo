package cord.eoeo.momentwo.data

object MomentwoApi {
    const val BASE_URL = "http://125.242.231.153:8080"

    // Sign Up
    const val POST_SIGN_UP = "/signup"
    const val POST_CHECK_EMAIL = "/check_email"
    const val POST_CHECK_NICKNAME = "/check_nickname"

    // Login
    const val POST_LOGIN = "/signin"

    // Album
    const val POST_CREATE_ALBUM = "/albums/create"
    /** Require @Path("id") */
    const val DELETE_ALBUM = "/albums/delete/{id}"
    const val PUT_ALBUM_IMAGE = "/albums/profile/image/upload"
    /** Require @Path("albumId") */
    const val DELETE_ALBUM_IMAGE = "/albums/profile/image/{albumId}"
    const val PUT_ALBUM_SUBTITLE = "/albums/subtitle"
    /** Require @Path("albumId") */
    const val DELETE_ALBUM_SUBTITLE = "/albums/subtitle/{albumId}"
    /** Require @Path("id") */
    const val PUT_ALBUM_TITLE = "/albums/edit/{id}"

    // Friend
    const val POST_FRIEND_REQUEST = "/friendship/request"
    const val DELETE_FRIEND_REQUEST = "/friendship/request/cancel"
    const val POST_RESPONSE_FRIEND_REQUEST = "/friendship/response"
    const val GET_FRIEND_LIST = "/friendship"
    const val GET_SENT_FRIEND_REQUESTS = "/friendship/send"
    const val GET_RECEIVED_FRIEND_REQUESTS = "/friendship/receive"
}
