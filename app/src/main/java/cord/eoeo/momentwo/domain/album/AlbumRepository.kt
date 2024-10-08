package cord.eoeo.momentwo.domain.album

import android.net.Uri
import cord.eoeo.momentwo.ui.model.AlbumItem
import cord.eoeo.momentwo.ui.model.MemberAuth

interface AlbumRepository {
    suspend fun requestCreateAlbum(
        title: String,
        inviteList: List<String>,
    ): Result<Unit>

    suspend fun deleteAlbum(albumId: Int): Result<Unit>

    suspend fun changeAlbumImage(
        albumId: Int,
        profileImage: Uri,
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

    suspend fun getAlbumList(): Result<List<AlbumItem>>

    suspend fun getAlbumRole(albumId: Int): Result<MemberAuth>
}
