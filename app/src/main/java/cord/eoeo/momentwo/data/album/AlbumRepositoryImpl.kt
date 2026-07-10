package cord.eoeo.momentwo.data.album

import android.content.Context
import android.net.Uri
import android.util.Log
import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.data.model.EditAlbumTitle
import cord.eoeo.momentwo.data.model.PresignedRequest
import cord.eoeo.momentwo.data.model.UriRequestBody
import cord.eoeo.momentwo.data.presigned.PresignedDataSource
import cord.eoeo.momentwo.domain.album.AlbumRepository
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
    ): Result<Unit> {
        val uriRequestBody = UriRequestBody(applicationContext.contentResolver, profileImage)

        albumRemoteDataSource
            .requestPresignedUrl(
                PresignedRequest(albumId, uriRequestBody.contentType()?.subtype ?: ""),
            ).map { it.presignedUrl }
            .onSuccess { presignedUrl ->
                val filename =
                    presignedUrl
                        .split("?")
                        .first()
                        .split("/")
                        .drop(3)
                        .joinToString("/")
                Log.d("Album", "filename: $filename")

                presignedRemoteDataSource
                    .uploadPhoto(presignedUrl, uriRequestBody)
                    .onSuccess {
                        return albumRemoteDataSource.changeAlbumImage(AlbumImage(albumId, filename))
                    }.onFailure {
                        return Result.failure(Exception("Presigned Upload Failure"))
                    }
            }.onFailure {
                return Result.failure(Exception("Presigned URL Failure"))
            }
        return Result.failure(Exception("Upload Photo Failure: Unknown Error"))
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
