package cord.eoeo.momentwo.data.subalbum

import cord.eoeo.momentwo.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.data.model.SubAlbumIds
import cord.eoeo.momentwo.ui.model.SubAlbumItem

class SubAlbumRepositoryImpl(
    private val subAlbumRemoteDataSource: SubAlbumDataSource,
) : SubAlbumRepository {
    override suspend fun requestCreateSubAlbum(
        albumId: Int,
        title: String,
    ): Result<Unit> {
        return subAlbumRemoteDataSource.requestCreateSubAlbum(CreateSubAlbumInfo(albumId, title))
        // return Result.success(Unit)
    }

    override suspend fun getSubAlbumList(albumId: Int): Result<List<SubAlbumItem>> {
        return subAlbumRemoteDataSource
            .getSubAlbumList(albumId)
            .map { subAlbumList ->
                subAlbumList.subAlbumList.map {
                    it.mapToSubAlbumItem()
                }
            }
        // return Result.success(emptyList())
    }

    override suspend fun changeSubAlbumTitle(
        albumId: Int,
        subAlbumId: Int,
        title: String,
    ): Result<Unit> {
        return subAlbumRemoteDataSource.changeSubAlbumTitle(EditSubAlbumInfo(albumId, subAlbumId, title))
        // return Result.success(Unit)
    }

    override suspend fun deleteSubAlbums(
        albumId: Int,
        subAlbumIds: List<Int>,
    ): Result<Unit> {
        return subAlbumRemoteDataSource.deleteSubAlbums(SubAlbumIds(albumId, subAlbumIds))
        // return Result.success(Unit)
    }
}
