package cord.eoeo.momentwo.core.data.photo

import androidx.paging.*
import cord.eoeo.momentwo.core.common.runSuspendCatching
import cord.eoeo.momentwo.core.database.PhotoEntity
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PhotoRemoteMediator private constructor(
    private val remote: PhotoDataSource.Remote,
    private val local: PhotoDataSource.Local,
    private val pageSize: Int,
    private val albumId: Int,
    private val subAlbumId: Int,
) : RemoteMediator<Int, PhotoEntity>() {
    class Factory(private val remote: PhotoDataSource.Remote, private val local: PhotoDataSource.Local) {
        fun create(pageSize: Int, albumId: Int, subAlbumId: Int) = PhotoRemoteMediator(remote, local, pageSize, albumId, subAlbumId)
    }
    override suspend fun initialize(): InitializeAction {
        val key = local.getLastKey(albumId, subAlbumId)
        return if (key == null || System.currentTimeMillis() - key.lastUpdated > TimeUnit.DAYS.toMillis(1))
            InitializeAction.LAUNCH_INITIAL_REFRESH else InitializeAction.SKIP_INITIAL_REFRESH
    }
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PhotoEntity>): MediatorResult = runSuspendCatching {
        if (loadType == LoadType.PREPEND) return@runSuspendCatching MediatorResult.Success(true)
        val previous = if (loadType == LoadType.APPEND) local.getLastKey(albumId, subAlbumId) else null
        if (loadType == LoadType.APPEND && previous?.nextCursor == null) return@runSuspendCatching MediatorResult.Success(true)
        val cursor = previous?.nextCursor ?: 0
        val page = remote.getPhotoPage(albumId, subAlbumId, pageSize, cursor).getOrThrow()
        val ids = page.images.map { it.id }
        val likes = if (ids.isEmpty()) emptyMap() else remote.getLikedPhoto(subAlbumId, ids.min(), ids.max()).getOrThrow()
            .likesList.associate { it.photoId to it.likesStatus }
        val photos = page.images.map { PhotoEntity(it.id, albumId, subAlbumId, it.imageUrl, likes[it.id] ?: false) }
        val end = if (page.nextCursor == null) maxOf(local.getLastPhotoId(albumId, subAlbumId) ?: 0, ids.maxOrNull() ?: 0)
            else ids.maxOrNull() ?: cursor
        // Fetch all required remote data first; rows and cursor then commit together in one scoped transaction.
        local.replacePage(albumId, subAlbumId, photos, cursor + 1, end,
            PhotoRemoteKeyEntity(albumId = albumId, subAlbumId = subAlbumId, lastUpdated = System.currentTimeMillis(), nextCursor = page.nextCursor),
            refresh = loadType == LoadType.REFRESH)
        MediatorResult.Success(endOfPaginationReached = page.nextCursor == null)
    }.getOrElse { MediatorResult.Error(it) }
}
