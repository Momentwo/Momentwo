package cord.eoeo.momentwo.data.photo.local

import androidx.paging.PagingSource
import cord.eoeo.momentwo.data.photo.PhotoDataSource
import cord.eoeo.momentwo.data.photo.local.entity.PhotoEntity
import cord.eoeo.momentwo.data.photo.local.entity.PhotoRemoteKeyEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoLocalDataSource(
    private val photoDao: PhotoDao,
    private val photoRemoteKeyDao: PhotoRemoteKeyDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PhotoDataSource.Local {
    override fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity> =
        photoDao.getPhotoPagingSource(albumId, subAlbumId)

    override suspend fun insertPhotos(photos: List<PhotoEntity>) {
        withContext(dispatcher) {
            photoDao.insertAll(*photos.toTypedArray())
        }
    }

    override suspend fun deletePhotos(photos: List<PhotoEntity>) {
        withContext(dispatcher) {
            photoDao.deleteAll(*photos.toTypedArray())
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
