package cord.eoeo.momentwo.core.data.like

import cord.eoeo.momentwo.core.data.model.LikeCount
import cord.eoeo.momentwo.core.data.model.LikeRequest

interface LikeDataSource {
    suspend fun requestDoLike(likeRequest: LikeRequest): Result<Unit>

    suspend fun requestUndoLike(likeRequest: LikeRequest): Result<Unit>

    suspend fun getLikeCount(albumId: Int, photoId: Int): Result<LikeCount>
}
