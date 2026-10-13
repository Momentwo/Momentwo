package cord.eoeo.momentwo.core.data.album

import cord.eoeo.momentwo.core.common.runSuspendCatching
import android.content.Context
import android.net.Uri
import cord.eoeo.momentwo.core.data.model.AlbumImage
import cord.eoeo.momentwo.core.data.model.AlbumSubTitle
import cord.eoeo.momentwo.core.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.core.data.model.EditAlbumTitle
import cord.eoeo.momentwo.core.data.model.PresignedRequest
import cord.eoeo.momentwo.core.data.model.UriRequestBody
import cord.eoeo.momentwo.core.data.presigned.PresignedDataSource
import cord.eoeo.momentwo.core.data.presigned.commitPresignedUpload
import cord.eoeo.momentwo.core.data.album.AlbumRepository
import cord.eoeo.momentwo.core.model.AlbumItem
import cord.eoeo.momentwo.core.model.MemberAuth

class AlbumRepositoryImpl(
    private val albumRemoteDataSource: AlbumDataSource,
    private val presignedRemoteDataSource: PresignedDataSource,
    private val applicationContext: Context,
) : AlbumRepository {
    override suspend fun requestCreateAlbum(
        title: String,
        inviteList: List<String>,
    ): Result<Unit> = albumRemoteDataSource.requestCreateAlbum(CreateAlbumInfo(title, inviteList))

    override suspend fun deleteAlbum(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbum(albumId)

    override suspend fun changeAlbumImage(
        albumId: Int,
        profileImage: Uri,
    ): Result<Unit> = runSuspendCatching {
        val body = UriRequestBody(applicationContext.contentResolver, profileImage)
        commitPresignedUpload(
            requestUrl = { albumRemoteDataSource.requestPresignedUrl(PresignedRequest(albumId, body.contentType()?.subtype ?: "")).map { it.presignedUrl } },
            upload = { url -> presignedRemoteDataSource.uploadPhoto(url, body) },
            commit = { filename -> albumRemoteDataSource.changeAlbumImage(AlbumImage(albumId, filename)) },
        ).getOrThrow()
    }

    override suspend fun deleteAlbumImage(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbumImage(albumId)

    override suspend fun changeAlbumSubTitle(
        albumId: Int,
        subTitle: String,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumSubTitle(AlbumSubTitle(albumId, subTitle))

    override suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbumSubTitle(albumId)

    override suspend fun changeAlbumTitle(
        albumId: Int,
        title: String,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumTitle(EditAlbumTitle(albumId, title))

    override suspend fun getAlbumList(): Result<List<AlbumItem>> =
        albumRemoteDataSource.getAlbumList().map { albumItemList ->
            albumItemList.albumList.map { it.mapToAlbumItem() }
        }

    override suspend fun getAlbumRole(albumId: Int): Result<MemberAuth> =
        albumRemoteDataSource.getAlbumRole(albumId).map { albumRole ->
            MemberAuth.fromRoleString(albumRole.rules)
        }
}
