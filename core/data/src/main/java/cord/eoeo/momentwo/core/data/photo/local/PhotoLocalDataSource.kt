package cord.eoeo.momentwo.core.data.photo.local

import cord.eoeo.momentwo.core.common.runSuspendCatching

import androidx.room.withTransaction
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import android.graphics.Bitmap
import androidx.paging.PagingSource
import cord.eoeo.momentwo.core.data.photo.PhotoDataSource
import cord.eoeo.momentwo.core.database.PhotoDao
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyDao
import cord.eoeo.momentwo.core.database.PhotoEntity
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.OutputStream

class PhotoLocalDataSource(
    private val photoDao: PhotoDao,
    private val photoRemoteKeyDao: PhotoRemoteKeyDao,
    private val dispatcher: CoroutineDispatcher,
    private val database: MomentwoDatabase,
    private val ownerId: String = "",
) : PhotoDataSource.Local {
    override fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity> =
        photoDao.getPhotoPagingSource(albumId, subAlbumId,ownerId)

    override suspend fun getLastPhotoId(albumId: Int, subAlbumId: Int): Int? =
        withContext(dispatcher) {
            photoDao.getLastPhotoId(albumId, subAlbumId,ownerId)
        }

    override suspend fun insertPhotos(photos: List<PhotoEntity>) {
        withContext(dispatcher) {
            photoDao.insertAll(*photos.map { it.copy(ownerId = ownerId) }.toTypedArray())
        }
    }

    override suspend fun deletePhotos(photoIds: List<Int>) {
        withContext(dispatcher) {
            photoDao.deleteByIds(photoIds,ownerId)
        }
    }

    override suspend fun deleteByRange(photoIds: List<Int>, start: Int, end: Int, albumId: Int, subAlbumId: Int) {
        withContext(dispatcher) {
            photoDao.deleteByRange(photoIds, start, end, albumId, subAlbumId,ownerId)
        }
    }

    override suspend fun updateIsLiked(photoId: Int, isLiked: Boolean) {
        withContext(dispatcher) {
            photoDao.updateIsLiked(photoId, isLiked,ownerId)
        }
    }

    override suspend fun downloadPhoto(bitmap: Bitmap, outputStream: OutputStream): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.close()
                }
            }
        }

    override suspend fun getLastKey(albumId: Int, subAlbumId: Int): PhotoRemoteKeyEntity? {
        return withContext(dispatcher) {
            photoRemoteKeyDao.getLastKey(albumId, subAlbumId,ownerId)
        }
    }

    override suspend fun insertKey(key: PhotoRemoteKeyEntity) {
        withContext(dispatcher) {
            photoRemoteKeyDao.insertKey(key.copy(ownerId = ownerId))
        }
    }

    override suspend fun clearKeys(albumId: Int, subAlbumId: Int) {
        withContext(dispatcher) {
            photoRemoteKeyDao.clearKeys(albumId, subAlbumId,ownerId)
        }
    }
    override suspend fun replacePage(albumId: Int, subAlbumId: Int, photos: List<PhotoEntity>, start: Int, end: Int, key: PhotoRemoteKeyEntity, refresh: Boolean) {
        withContext(dispatcher) {
            database.withTransaction {
                if (refresh) photoRemoteKeyDao.clearKeys(albumId, subAlbumId,ownerId)
                photoDao.deleteByRange(photos.map { it.id }, start, end, albumId, subAlbumId,ownerId)
                photoDao.insertAll(*photos.map { it.copy(ownerId = ownerId) }.toTypedArray())
                photoRemoteKeyDao.insertKey(key.copy(ownerId = ownerId))
            }
        }
    }
}
