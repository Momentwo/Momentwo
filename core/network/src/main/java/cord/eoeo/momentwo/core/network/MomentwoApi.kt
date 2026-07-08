package cord.eoeo.momentwo.core.network

import cord.eoeo.momentwo.core.network.BuildConfig

object MomentwoApi {
    const val BASE_URL = BuildConfig.BASE_URL

    // Sign Up
    const val POST_SIGN_UP = "/signup"
    const val POST_CHECK_EMAIL = "/check_email"
    const val POST_CHECK_NICKNAME = "/check_nickname"

    // Login
    const val POST_LOGIN = "/signin"

    // Profile
    /** Require @Query("nickname") */
    const val GET_PROFILE = "/users/profiles"

    // Album
    const val POST_CREATE_ALBUM = "/albums"
    /** Require @Path("albumId") */
    const val DELETE_ALBUM = "/albums/{albumId}"
    const val POST_ALBUM_IMAGE = "/albums/profile/image"
    /** Require @Path("albumId") */
    const val DELETE_ALBUM_IMAGE = "/albums/{albumId}/profile/image"
    const val PUT_ALBUM_SUBTITLE = "/albums/subtitle"
    /** Require @Path("albumId") */
    const val DELETE_ALBUM_SUBTITLE = "/albums/{albumId}/subtitle"
    const val PUT_ALBUM_TITLE = "/albums"
    const val GET_ALBUM_LIST = "/albums"
    /** Require @Path("albumId") */
    const val GET_ALBUM_ROLE = "/albums/rules/{albumId}"
    const val POST_ALBUM_PRESIGNED = "/images/albums/profiles/presigned"

    // SubAlbum
    const val POST_CREATE_SUBALBUM = "/albums/sub"
    /** Require @Path("albumId") */
    const val GET_SUBALBUM_LIST = "/albums/{albumId}/sub"
    const val PUT_EDIT_SUBALBUM = "/albums/sub/title"
    /** Require @Path("albumId"), @Path("subAlbumIds") */
    const val DELETE_SUBALBUMS = "/albums/{albumId}/sub/{subAlbumIds}"

    // Member
    /** Require @Path("albumId") */
    const val DELETE_EXIT_MEMBER = "/albums/{albumId}/members/self"
    const val POST_INVITE_MEMBERS = "/albums/members/invite"
    /** Require @Path("albumId") */
    const val GET_MEMBER_LIST = "/albums/{albumId}/members"
    /** Require @Path("albumId"), @Path("kickUsersId") */
    const val DELETE_KICK_MEMBERS = "/albums/{albumId}/members/kick/{kickUsersId}"
    const val PUT_MEMBER_ASSIGN_ADMIN = "/albums/members/assign/admin"
    const val PUT_EDIT_MEMBERS_PERMISSION = "/albums/members/permission"

    // Photo
    const val POST_PHOTO_UPLOAD = "/albums/sub/photos"
    /** Require @Path("albumId"), @Path("subAlbumId"), @Path("imagesId") */
    const val DELETE_PHOTOS = "/albums/{albumId}/sub/{subAlbumId}/photos/{imagesId}"
    /** Require @Path("albumId"), @Path("subAlbumId"), @Query("size"), @Query("cursor") */
    const val GET_PHOTO_PAGE = "/albums/{albumId}/sub/{subAlbumId}/photos"
    const val PUT_MOVE_PHOTO = "/albums/sub/photos/move" // TODO: 사진 이동 기능 추가
    const val POST_PHOTO_PRESIGNED = "/images/photos/presigned"
    /** Require @Query("subAlbumId"), @Query("minPid"), @Query("maxPid") */
    const val GET_LIKED_PHOTOS = "/photo/likes/status/search"

    // Description
    const val POST_CREATE_DESCRIPTION = "/albums/sub/photos/descriptions"
    const val PUT_EDIT_DESCRIPTION = "/albums/sub/photos/descriptions"
    /** Require @Path("albumId"), @Path("photoId") */
    const val DELETE_DESCRIPTION = "/albums/{albumId}/sub/photos/{photoId}/descriptions"
    /** Require @Path("albumId"), @Path("photoId") */
    const val GET_DESCRIPTION = "/albums/{albumId}/sub/photos/{photoId}/descriptions"

    // Comment
    const val POST_CREATE_COMMENT = "/albums/sub/photos/comments"
    const val PUT_EDIT_COMMENT = "/albums/sub/photos/comments"
    /** Require @Path("albumId"), @Path("commentId") */
    const val DELETE_COMMENT = "/albums/{albumId}/sub/photos/comments/{commentId}"
    /** Require @Path("albumId"), @Path("photoId"), @Query("size"), @Query("cursor") */
    const val GET_COMMENT_PAGE = "/albums/{albumId}/sub/photos/{photoId}/comments"

    // Like
    const val POST_DO_LIKE = "/photo/likes/do"
    const val POST_UNDO_LIKE = "/photo/likes/undo"
    /** Require @Path("albumId"), @Path("photoId") */
    const val GET_LIKE_COUNT = "/photo/likes/{albumId}/{photoId}"

    // Friend
    const val POST_FRIEND_REQUEST = "/friendship/request"
    const val POST_RESPONSE_FRIEND_REQUEST = "/friendship/response"
    /** Require @Path("userId") */
    const val DELETE_FRIEND = "/friendship/user/{userId}" // TODO: 친구 삭제 기능 추가
    /** Require @Path("userId") */
    const val DELETE_FRIEND_REQUEST = "/friendship/user/{userId}/request"
    /** Require @Query("size"), @Query("cursor") */
    const val GET_FRIEND_PAGE = "/friendship"
    const val GET_SENT_FRIEND_REQUESTS = "/friendship/send"
    const val GET_RECEIVED_FRIEND_REQUESTS = "/friendship/receive"
    /** Require @Query("nickname"), @Query("page"), @Query("size") */
    const val GET_SEARCH_USERS = "/users/search"
}
