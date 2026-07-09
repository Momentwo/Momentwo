package cord.eoeo.momentwo.core.data.album

import cord.eoeo.momentwo.core.data.model.AlbumImage
import cord.eoeo.momentwo.core.data.model.AlbumInfoList
import cord.eoeo.momentwo.core.data.model.AlbumRole
import cord.eoeo.momentwo.core.data.model.AlbumSubTitle
import cord.eoeo.momentwo.core.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.core.data.model.EditAlbumTitle
import cord.eoeo.momentwo.core.data.model.PresignedRequest
import cord.eoeo.momentwo.core.data.model.PresignedUrl

interface AlbumDataSource {
    suspend fun requestCreateAlbum(createAlbumInfo: CreateAlbumInfo): Result<Unit>

    suspend fun deleteAlbum(albumId: Int): Result<Unit>

    suspend fun requestPresignedUrl(presignedRequest: PresignedRequest): Result<PresignedUrl>

    suspend fun changeAlbumImage(albumImage: AlbumImage): Result<Unit>

    suspend fun deleteAlbumImage(albumId: Int): Result<Unit>

    suspend fun changeAlbumSubTitle(albumSubTitle: AlbumSubTitle): Result<Unit>

    suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit>

    suspend fun changeAlbumTitle(editTitle: EditAlbumTitle): Result<Unit>

    suspend fun getAlbumList(): Result<AlbumInfoList>

    suspend fun getAlbumRole(albumId: Int): Result<AlbumRole>
}
