package cord.eoeo.momentwo.core.data.photo

import android.graphics.Bitmap
import androidx.paging.*
import cord.eoeo.momentwo.core.data.model.*
import cord.eoeo.momentwo.core.database.*
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class PhotoRemoteMediatorTest {
    private val local = PhotoCacheFake()
    private val remote = PhotoRemoteFake()
    private val factory = PhotoRemoteMediator.Factory(remote, local)
    private fun state() = PagingState<Int, PhotoEntity>(emptyList(), null, PagingConfig(10), 0)
    @Test fun terminalPagePersistsImagesAndRealLikeStatus() = runTest {
        remote.page = Result.success(PhotoPage(listOf(PhotoInfo(1, "last")), null))
        remote.likes = Result.success(LikedPhotoList(listOf(LikedPhoto(1, 2, 1, "fixture", false))))
        val result = factory.create(10, 1, 2).load(LoadType.REFRESH, state()) as RemoteMediator.MediatorResult.Success
        assertTrue(result.endOfPaginationReached)
        assertEquals(listOf(PhotoEntity(1, 1, 2, "last", false)), local.photos)
        assertNull(local.keys[1 to 2]?.nextCursor)
    }
    @Test fun emptyRefreshClearsOnlyCurrentAlbumAndSkipsLikeQuery() = runTest {
        local.photos += PhotoEntity(1, 1, 2, "old", false)
        local.photos += PhotoEntity(50, 3, 4, "other", true)
        local.keys[3 to 4] = PhotoRemoteKeyEntity(albumId = 3, subAlbumId = 4, lastUpdated = 1, nextCursor = 50)
        remote.page = Result.success(PhotoPage(emptyList(), null))
        factory.create(10, 1, 2).load(LoadType.REFRESH, state())
        assertEquals(listOf(PhotoEntity(50, 3, 4, "other", true)), local.photos)
        assertEquals(50, local.keys[3 to 4]?.nextCursor)
        assertEquals(0, remote.likeCalls)
    }
    @Test fun appendStoresLastPageWithoutDeletingEarlierPages() = runTest {
        local.photos += PhotoEntity(1, 1, 2, "earlier", false)
        local.keys[1 to 2] = PhotoRemoteKeyEntity(albumId = 1, subAlbumId = 2, lastUpdated = 1, nextCursor = 1)
        remote.page = Result.success(PhotoPage(listOf(PhotoInfo(2, "last")), null))
        factory.create(10, 1, 2).load(LoadType.APPEND, state())
        assertEquals(listOf(1, 2), local.photos.map { it.id })
        assertNull(local.keys[1 to 2]?.nextCursor)
    }
    @Test fun cancellationCannotBecomeMediatorErrorOrReplaceCache() = runTest {
        local.photos += PhotoEntity(1, 1, 2, "old", false)
        remote.page = Result.failure(CancellationException("cancelled"))
        try { factory.create(10, 1, 2).load(LoadType.REFRESH, state()); fail("Expected cancellation") } catch (_: CancellationException) { }
        assertEquals("old", local.photos.single().photoUrl)
        assertEquals(0, local.replacements)
    }
    @Test fun independentPagersKeepTheirOwnParameters() = runTest {
        val first = factory.create(10, 1, 2)
        val second = factory.create(20, 3, 4)
        first.load(LoadType.REFRESH, state())
        second.load(LoadType.REFRESH, state())
        assertEquals(listOf(Triple(1, 2, 10), Triple(3, 4, 20)), remote.calls)
    }
    @Test fun failedLikeLookupDoesNotClearExistingCache() = runTest {
        local.photos += PhotoEntity(1, 1, 2, "old", false)
        remote.page = Result.success(PhotoPage(listOf(PhotoInfo(2, "new")), null))
        remote.likes = Result.failure(java.io.IOException("offline"))
        assertTrue(factory.create(10, 1, 2).load(LoadType.REFRESH, state()) is RemoteMediator.MediatorResult.Error)
        assertEquals("old", local.photos.single().photoUrl)
        assertEquals(0, local.replacements)
    }
}
private class PhotoCacheFake : PhotoDataSource.Local {
    val photos = mutableListOf<PhotoEntity>()
    val keys = mutableMapOf<Pair<Int, Int>, PhotoRemoteKeyEntity>()
    var replacements = 0
    override fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity> = error("unused")
    override suspend fun getLastPhotoId(albumId: Int, subAlbumId: Int) = photos.filter { it.albumId == albumId && it.subAlbumId == subAlbumId }.maxOfOrNull { it.id }
    override suspend fun insertPhotos(photos: List<PhotoEntity>) { this.photos += photos }
    override suspend fun deletePhotos(photoIds: List<Int>) { photos.removeAll { it.id in photoIds } }
    override suspend fun deleteByRange(photoIds: List<Int>, start: Int, end: Int, albumId: Int, subAlbumId: Int) {
        photos.removeAll { it.albumId == albumId && it.subAlbumId == subAlbumId && it.id in start..end && it.id !in photoIds }
    }
    override suspend fun updateIsLiked(photoId: Int, isLiked: Boolean) = Unit
    override suspend fun downloadPhoto(bitmap: Bitmap, outputStream: OutputStream) = Result.success(Unit)
    override suspend fun getLastKey(albumId: Int, subAlbumId: Int) = keys[albumId to subAlbumId]
    override suspend fun insertKey(key: PhotoRemoteKeyEntity) { keys[key.albumId to key.subAlbumId] = key }
    override suspend fun clearKeys(albumId: Int, subAlbumId: Int) { keys.remove(albumId to subAlbumId) }
    override suspend fun replacePage(albumId: Int, subAlbumId: Int, photos: List<PhotoEntity>, start: Int, end: Int, key: PhotoRemoteKeyEntity, refresh: Boolean) {
        replacements++
        if (refresh) clearKeys(albumId, subAlbumId)
        deleteByRange(photos.map { it.id }, start, end, albumId, subAlbumId)
        insertPhotos(photos)
        insertKey(key)
    }
}
private class PhotoRemoteFake : PhotoDataSource.Remote {
    var page: Result<PhotoPage> = Result.success(PhotoPage(emptyList(), null))
    var likes: Result<LikedPhotoList> = Result.success(LikedPhotoList(emptyList()))
    val calls = mutableListOf<Triple<Int, Int, Int>>()
    var likeCalls = 0
    override suspend fun getPhotoPage(albumId: Int, subAlbumId: Int, pageSize: Int, cursor: Int): Result<PhotoPage> { calls += Triple(albumId, subAlbumId, pageSize); return page }
    override suspend fun getLikedPhoto(subAlbumId: Int, minPid: Int, maxPid: Int): Result<LikedPhotoList> { likeCalls++; return likes }
    override suspend fun requestPresignedUrl(presignedRequest: PresignedRequest): Result<PresignedUrl> = error("unused")
    override suspend fun requestUploadPhoto(uploadPhoto: UploadPhoto) = Result.success(Unit)
    override suspend fun deletePhotos(albumId: Int, subAlbumId: Int, imageIds: String) = Result.success(Unit)
}
