package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
