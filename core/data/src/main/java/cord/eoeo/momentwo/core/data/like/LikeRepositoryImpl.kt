package cord.eoeo.momentwo.core.data.like

import cord.eoeo.momentwo.core.data.model.LikeRequest

class LikeRepositoryImpl(
    private val likeRemoteDataSource: LikeDataSource,
) : LikeRepository {
    override suspend fun requestDoLike(albumId: Int, photoId: Int): Result<Unit> =
        likeRemoteDataSource.requestDoLike(LikeRequest(albumId, photoId))

    override suspend fun requestUndoLike(albumId: Int, photoId: Int): Result<Unit> =
        likeRemoteDataSource.requestUndoLike(LikeRequest(albumId, photoId))

    override suspend fun getLikeCount(albumId: Int, photoId: Int): Result<Int> =
        likeRemoteDataSource.getLikeCount(albumId, photoId).map { it.count }
}
