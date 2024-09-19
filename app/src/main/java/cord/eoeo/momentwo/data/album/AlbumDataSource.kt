package cord.eoeo.momentwo.data.album

import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumInfoList
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo

interface AlbumDataSource {
    suspend fun requestCreateAlbum(createAlbumInfo: CreateAlbumInfo): Result<Unit>

    suspend fun deleteAlbum(albumId: Int): Result<Unit>

    suspend fun changeAlbumImage(albumImage: AlbumImage): Result<Unit>

    suspend fun deleteAlbumImage(albumId: Int): Result<Unit>

    suspend fun changeAlbumSubTitle(albumSubTitle: AlbumSubTitle): Result<Unit>

    suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit>

    suspend fun changeAlbumTitle(
        albumId: Int,
        editTitle: String,
    ): Result<Unit>

    suspend fun getAlbumList(): Result<AlbumInfoList>
}
