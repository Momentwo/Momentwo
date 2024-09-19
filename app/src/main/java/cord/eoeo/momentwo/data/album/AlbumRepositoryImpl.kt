package cord.eoeo.momentwo.data.album

import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.domain.album.AlbumRepository
import cord.eoeo.momentwo.ui.model.AlbumItem
import okhttp3.MultipartBody

class AlbumRepositoryImpl(
    private val albumRemoteDataSource: AlbumDataSource,
) : AlbumRepository {
    override suspend fun requestCreateAlbum(
        title: String,
        inviteList: List<String>,
    ): Result<Unit> = albumRemoteDataSource.requestCreateAlbum(CreateAlbumInfo(title, inviteList))

    override suspend fun deleteAlbum(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbum(albumId)

    override suspend fun changeAlbumImage(
        albumId: String,
        profileImage: MultipartBody.Part,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumImage(AlbumImage(albumId, profileImage))

    override suspend fun deleteAlbumImage(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbumImage(albumId)

    override suspend fun changeAlbumSubTitle(
        albumId: Int,
        subTitle: String,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumSubTitle(AlbumSubTitle(albumId, subTitle))

    override suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbumSubTitle(albumId)

    override suspend fun changeAlbumTitle(
        albumId: Int,
        title: String,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumTitle(albumId, title)

    override suspend fun getAlbumList(): Result<List<AlbumItem>> =
        albumRemoteDataSource.getAlbumList().map { albumItemList ->
            albumItemList.albumList.map { it.mapToAlbumItem() }
        }
}
