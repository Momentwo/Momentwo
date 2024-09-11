package cord.eoeo.momentwo.data.subalbum

import cord.eoeo.momentwo.ui.model.SubAlbumItem

interface SubAlbumRepository {
    suspend fun requestCreateSubAlbum(
        albumId: Int,
        title: String,
    ): Result<Unit>

    suspend fun getSubAlbumList(albumId: Int): Result<List<SubAlbumItem>>

    suspend fun changeSubAlbumTitle(
        albumId: Int,
        subAlbumId: Int,
        title: String,
    ): Result<Unit>

    suspend fun deleteSubAlbums(
        albumId: Int,
        subAlbumIds: List<Int>,
    ): Result<Unit>
}
