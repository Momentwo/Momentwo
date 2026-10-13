package cord.eoeo.momentwo

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.photo.local.PhotoLocalDataSource
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.database.PhotoDao
import cord.eoeo.momentwo.core.database.PhotoEntity
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Real Room transactions in a separate in-memory database; production cache is untouched. */
@RunWith(AndroidJUnit4::class)
class PhotoCacheTransactionSmokeTest {
    private lateinit var db: MomentwoDatabase

    @Before fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, MomentwoDatabase::class.java).build()
    }
    @After fun closeDatabase() = db.close()

    @Test fun terminalRefreshReplacesOnlyItsAlbumAndSubAlbum() = runBlocking {
        db.photoDao().insertAll(PhotoEntity(1, 10, 20, "old", false), PhotoEntity(50, 30, 40, "other", true))
        db.photoRemoteKeyDao().insertKey(PhotoRemoteKeyEntity(albumId = 10, subAlbumId = 20, lastUpdated = 1, nextCursor = 1))
        db.photoRemoteKeyDao().insertKey(PhotoRemoteKeyEntity(albumId = 30, subAlbumId = 40, lastUpdated = 1, nextCursor = 50))
        val source = PhotoLocalDataSource(db.photoDao(), db.photoRemoteKeyDao(), Dispatchers.IO, db)
        source.replacePage(10, 20, listOf(PhotoEntity(2, 10, 20, "terminal", false)), 1, 50,
            PhotoRemoteKeyEntity(albumId = 10, subAlbumId = 20, lastUpdated = 2, nextCursor = null), true)
        assertEquals(2, db.photoDao().getLastPhotoId(10, 20))
        assertNull(db.photoRemoteKeyDao().getLastKey(10, 20)!!.nextCursor)
        assertEquals(50, db.photoDao().getLastPhotoId(30, 40))
        assertEquals(50, db.photoRemoteKeyDao().getLastKey(30, 40)!!.nextCursor)
    }

    @Test fun cancellationAfterDeleteRollsBackRowsAndCursorTogether() = runBlocking {
        val realDao = db.photoDao()
        realDao.insertAll(PhotoEntity(1, 10, 20, "old", false))
        db.photoRemoteKeyDao().insertKey(PhotoRemoteKeyEntity(albumId = 10, subAlbumId = 20, lastUpdated = 1, nextCursor = 1))
        val cancellingDao = object : PhotoDao by realDao {
            override suspend fun deleteByRange(photoIds: List<Int>, start: Int, end: Int, albumId: Int, subAlbumId: Int) {
                realDao.deleteByRange(photoIds, start, end, albumId, subAlbumId)
                currentCoroutineContext().cancel()
                yield()
            }
        }
        val source = PhotoLocalDataSource(cancellingDao, db.photoRemoteKeyDao(), Dispatchers.IO, db)
        val job = launch {
            source.replacePage(10, 20, listOf(PhotoEntity(2, 10, 20, "new", true)), 1, 50,
                PhotoRemoteKeyEntity(albumId = 10, subAlbumId = 20, lastUpdated = 2, nextCursor = null), true)
        }
        job.join()
        assertTrue(job.isCancelled)
        assertEquals(1, realDao.getLastPhotoId(10, 20))
        assertEquals(1, db.photoRemoteKeyDao().getLastKey(10, 20)!!.nextCursor)
        assertEquals(1L, db.photoRemoteKeyDao().getLastKey(10, 20)!!.lastUpdated)
    }
}
