package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.ui.model.AlbumItem
import okhttp3.MultipartBody
import retrofit2.http.Part

@JsonClass(generateAdapter = true)
data class CreateAlbumInfo(
    @Json(name = "createTitle")
    val title: String,
    @Json(name = "doInviteNicknameList")
    val inviteList: List<String>,
)

@JsonClass(generateAdapter = true)
data class AlbumImage(
    val albumId: Int,
    @Part val profileImage: MultipartBody.Part,
)

@JsonClass(generateAdapter = true)
data class AlbumSubTitle(
    val albumId: Int,
    val subTitle: String,
)

@JsonClass(generateAdapter = true)
data class EditAlbumTitle(
    val albumId: Int,
    val editTitle: String,
)

@JsonClass(generateAdapter = true)
data class AlbumInfo(
    val id: Int,
    val title: String,
    val subTitle: String,
    val profileImage: String,
) {
    fun mapToAlbumItem(): AlbumItem =
        AlbumItem(
            id = id,
            title = title,
            subTitle = subTitle,
            imageUrl = profileImage,
        )
}

@JsonClass(generateAdapter = true)
data class AlbumInfoList(
    @Json(name = "albumInfoList")
    val albumList: List<AlbumInfo>,
)

@JsonClass(generateAdapter = true)
data class AlbumRole(
    val albumId: Int,
    val rules: MemberRule,
)
