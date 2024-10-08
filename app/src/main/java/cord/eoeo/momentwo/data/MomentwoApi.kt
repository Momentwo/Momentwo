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
    const val GET_ALBUM_LIST = "/albums"
    /** Require @Path("albumId") */
    const val GET_ALBUM_ROLE = "/albums/rules/{albumId}"

    // SubAlbum
    const val POST_CREATE_SUBALBUM = "/album/sub/create"
    /** Require @Path("albumId") */
    const val GET_SUBALBUM_LIST = "/album/sub/{albumId}"
    const val PUT_EDIT_SUBALBUM = "/album/sub/edit"
    const val DELETE_SUBALBUMS = "/album/sub/delete"

    // Member
    /** Require @Path("albumId") */
    const val DELETE_EXIT_MEMBER = "/{albumId}/members/out"
    /** Require @Path("albumId") */
    const val POST_INVITE_MEMBERS = "/{albumId}/members/invite"
    /** Require @Path("albumId") */
    const val GET_MEMBER_LIST = "/{albumId}/members"
    /** Require @Path("albumId") */
    const val DELETE_KICK_MEMBERS = "/{albumId}/members/kick"
    /** Require @Path("albumId") */
    const val PUT_MEMBER_ASSIGN_ADMIN = "/{albumId}/members/assign/admin"
    /** Require @Path("albumId") */
    const val PUT_EDIT_MEMBERS_PERMISSION = "/{albumId}/members/permission"

    // Friend
    const val POST_FRIEND_REQUEST = "/friendship/request"
    const val DELETE_FRIEND_REQUEST = "/friendship/request/cancel"
    const val POST_RESPONSE_FRIEND_REQUEST = "/friendship/response"
    const val GET_FRIEND_LIST = "/friendship"
    const val GET_SENT_FRIEND_REQUESTS = "/friendship/send"
    const val GET_RECEIVED_FRIEND_REQUESTS = "/friendship/receive"
}
