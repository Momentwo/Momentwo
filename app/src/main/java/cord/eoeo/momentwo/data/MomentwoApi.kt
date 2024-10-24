package cord.eoeo.momentwo.data

import cord.eoeo.momentwo.BuildConfig

object MomentwoApi {
    const val BASE_URL = BuildConfig.BASE_URL

    // Sign Up
    const val POST_SIGN_UP = "/signup"
    const val POST_CHECK_EMAIL = "/check_email"
    const val POST_CHECK_NICKNAME = "/check_nickname"

    // Login
    const val POST_LOGIN = "/signin"

    // Album
    const val POST_CREATE_ALBUM = "/albums/create"
    const val DELETE_ALBUM = "/albums/delete"
    const val PUT_ALBUM_IMAGE = "/albums/profile/image/upload"
    const val DELETE_ALBUM_IMAGE = "/albums/profile/image"
    const val PUT_ALBUM_SUBTITLE = "/albums/subtitle"
    const val DELETE_ALBUM_SUBTITLE = "/albums/subtitle"
    const val PUT_ALBUM_TITLE = "/albums/edit"
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
    const val DELETE_EXIT_MEMBER = "/members/out"
    const val POST_INVITE_MEMBERS = "/members/invite"
    /** Require @Path("albumId") */
    const val GET_MEMBER_LIST = "/members/{albumId}"
    const val DELETE_KICK_MEMBERS = "/members/kick"
    const val PUT_MEMBER_ASSIGN_ADMIN = "/members/assign/admin"
    const val PUT_EDIT_MEMBERS_PERMISSION = "/members/permission"

    // Photo
    const val DELETE_PHOTOS = "/photos/delete"
    /** Require @Path("albumId"), @Path("subAlbumId"), @Query("cursor") */
    const val GET_PHOTO_PAGE = "/photos/view/{albumId}/{subAlbumId}"
    const val POST_PHOTO_PRESIGNED = "/images/photos/presigned"
    const val POST_PHOTO_UPLOAD = "/photos/upload"

    // Friend
    const val POST_FRIEND_REQUEST = "/friendship/request"
    const val DELETE_FRIEND_REQUEST = "/friendship/request/cancel"
    const val POST_RESPONSE_FRIEND_REQUEST = "/friendship/response"
    const val GET_FRIEND_LIST = "/friendship"
    const val GET_SENT_FRIEND_REQUESTS = "/friendship/send"
    const val GET_RECEIVED_FRIEND_REQUESTS = "/friendship/receive"
}
