package cord.eoeo.momentwo.core.data.photo.local

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
    private val dispatcher: CoroutineDispatcher
) : PhotoDataSource.Local {
    override fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity> =
        photoDao.getPhotoPagingSource(albumId, subAlbumId)

    override suspend fun getLastPhotoId(): Int? =
        withContext(dispatcher) {
            photoDao.getLastPhotoId()
        }

    override suspend fun insertPhotos(photos: List<PhotoEntity>) {
        withContext(dispatcher) {
            photoDao.insertAll(*photos.toTypedArray())
        }
    }

    override suspend fun deletePhotos(photoIds: List<Int>) {
        withContext(dispatcher) {
            photoDao.deleteByIds(photoIds)
        }
    }

    override suspend fun deleteByRange(photoIds: List<Int>, start: Int, end: Int) {
        withContext(dispatcher) {
            photoDao.deleteByRange(photoIds, start, end)
        }
    }

    override suspend fun updateIsLiked(photoId: Int, isLiked: Boolean) {
        withContext(dispatcher) {
            photoDao.updateIsLiked(photoId, isLiked)
        }
    }

    override suspend fun downloadPhoto(bitmap: Bitmap, outputStream: OutputStream): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.close()
                }
            }
        }

    override suspend fun getLastKey(albumId: Int, subAlbumId: Int): PhotoRemoteKeyEntity? {
        return withContext(dispatcher) {
            photoRemoteKeyDao.getLastKey(albumId, subAlbumId)
        }
    }

    override suspend fun insertKey(key: PhotoRemoteKeyEntity) {
        withContext(dispatcher) {
            photoRemoteKeyDao.insertKey(key)
        }
    }

    override suspend fun clearKeys() {
        withContext(dispatcher) {
            photoRemoteKeyDao.clearKeys()
        }
    }
}
