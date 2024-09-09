package cord.eoeo.momentwo.domain.album

import okhttp3.MultipartBody

interface AlbumRepository {
    suspend fun requestCreateAlbum(
        title: String,
        inviteList: List<String>,
    ): Result<Unit>

    suspend fun deleteAlbum(albumId: Int): Result<Unit>

    suspend fun changeAlbumImage(
        albumId: String,
        profileImage: MultipartBody.Part,
    ): Result<Unit>

    suspend fun deleteAlbumImage(albumId: Int): Result<Unit>

    suspend fun changeAlbumSubTitle(
        albumId: Int,
        subTitle: String,
    ): Result<Unit>

    suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit>

    suspend fun changeAlbumTitle(
        albumId: Int,
        title: String,
    ): Result<Unit>
}
