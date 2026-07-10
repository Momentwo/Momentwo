package cord.eoeo.momentwo.core.data.subalbum

import cord.eoeo.momentwo.core.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.core.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.core.data.subalbum.SubAlbumRepository
import cord.eoeo.momentwo.core.model.SubAlbumItem

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
        return subAlbumRemoteDataSource.deleteSubAlbums(albumId, subAlbumIds.joinToString(","))
        // return Result.success(Unit)
    }
}
