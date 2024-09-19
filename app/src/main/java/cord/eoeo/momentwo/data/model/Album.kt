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
    val albumId: String,
    @Part val profileImage: MultipartBody.Part,
)

@JsonClass(generateAdapter = true)
data class AlbumSubTitle(
    val albumId: Int,
    val subTitle: String,
)

@JsonClass(generateAdapter = true)
data class AlbumInfo(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
) {
    fun mapToAlbumItem(): AlbumItem =
        AlbumItem(
            id = id,
            title = title,
            subTitle = subtitle,
            imageUrl = imageUrl,
        )
}

@JsonClass(generateAdapter = true)
data class AlbumInfoList(
    @Json(name = "album")
    val albumList: List<AlbumInfo>,
)
