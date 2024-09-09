package cord.eoeo.momentwo.data.album

import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.AlbumTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.domain.album.AlbumRepository
import okhttp3.MultipartBody

class AlbumRepositoryImpl(
    private val albumRemoteDataSource: AlbumDataSource,
) : AlbumRepository {
    override suspend fun requestCreateAlbum(
        title: String,
        inviteList: List<String>,
    ): Result<Unit> {
        return albumRemoteDataSource.requestCreateAlbum(CreateAlbumInfo(title, inviteList))
    }

    override suspend fun deleteAlbum(albumId: Int): Result<Unit> {
        return albumRemoteDataSource.deleteAlbum(albumId)
    }

    override suspend fun changeAlbumImage(
        albumId: String,
        profileImage: MultipartBody.Part,
    ): Result<Unit> {
        return albumRemoteDataSource.changeAlbumImage(AlbumImage(albumId, profileImage))
    }

    override suspend fun deleteAlbumImage(albumId: Int): Result<Unit> {
        return albumRemoteDataSource.deleteAlbumImage(albumId)
    }

    override suspend fun changeAlbumSubTitle(
        albumId: Int,
        subTitle: String,
    ): Result<Unit> {
        return albumRemoteDataSource.changeAlbumSubTitle(AlbumSubTitle(albumId, subTitle))
    }

    override suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit> {
        return albumRemoteDataSource.deleteAlbumSubTitle(albumId)
    }

    override suspend fun changeAlbumTitle(
        albumId: Int,
        title: String,
    ): Result<Unit> {
        return albumRemoteDataSource.changeAlbumTitle(albumId, AlbumTitle(title))
    }
}
