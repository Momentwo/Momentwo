package cord.eoeo.momentwo

import android.content.Context
import android.content.ComponentName
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.LibraryTestAccess
import cord.eoeo.momentwo.core.data.library.sync.AccountSyncJobService
import cord.eoeo.momentwo.core.data.library.sync.PlatformSyncScheduling
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Run seed and verify in separate instrumentations with an external target force-stop between them. */
@RunWith(AndroidJUnit4::class)
class GuestProcessPersistenceSmokeTest {
    @Test fun processBoundaryPreservesOwnedFilesAndRecords()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val args=InstrumentationRegistry.getArguments()
        // A regular whole-suite run skips this staged check; the regression runner supplies its stage.
        val stage=args.getString("persistenceStage") ?: return@runBlocking
        val verifyScheduler=args.getString("verifyScheduler")=="true"
        if(verifyScheduler) {
            require(args.getString("requireOffline")=="true")
            awaitSdkRuntimeNetwork()
        }
        val access=EntryPointAccessors.fromApplication(context,LibraryTestAccess::class.java)
        val counter=EntryPointAccessors.fromApplication(context,ApiDiagnostics::class.java).apiRequests()
        access.preferences().clearSession();val before=counter.count
        val fixture=context.getSharedPreferences("process-persistence-fixture",Context.MODE_PRIVATE)
        val scheduler=context.getSystemService(JobScheduler::class.java)
        fun assertPersistedSyncJob() {
            val job=requireNotNull(scheduler.getPendingJob(162)) { "Persisted sync job must exist" }
            assertTrue(job.isPersisted)
            assertEquals(JobInfo.NETWORK_TYPE_ANY,job.networkType)
            assertEquals(ComponentName(context,AccountSyncJobService::class.java),job.service)
        }
        val repo=access.repository()
        when(stage) {
            "seed" -> {
                val source=File(context.cacheDir,"process-source.png")
                Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888).also{bitmap->bitmap.eraseColor(android.graphics.Color.BLUE);source.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
                val album=repo.createAlbum("Process original");val sub=repo.createSubAlbum(album,"Child");val photo=repo.importPhoto(sub,source.toUri())
                repo.setDescription(photo,"persisted description");repo.setFavorite(photo,true);repo.saveMemo(photo,"persisted memo")
                assertTrue(fixture.edit().putString("album",album).putString("sub",sub).putString("photo",photo).commit());assertTrue(source.delete())
                if(verifyScheduler) {
                    assertNull("Fixture must not replace an existing sync job",scheduler.getPendingJob(162))
                    PlatformSyncScheduling(context).enqueue()
                    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                    assertPersistedSyncJob()
                    assertTrue(fixture.edit().putBoolean("ownsSyncJob",true).commit())
                }
            }
            "verify" -> {
                context.cacheDir.listFiles()?.forEach{it.deleteRecursively()};repo.cleanUnusedMedia()
                val photo=repo.photo(requireNotNull(fixture.getString("photo",null))).first()!!
                assertEquals("persisted description",photo.description);assertTrue(photo.favorite)
                assertEquals("persisted memo",repo.memos(photo.localId).first().single().text)
                assertTrue(access.media().file(requireNotNull(photo.mediaName)).isFile)
                assertEquals("Process original",repo.album(requireNotNull(fixture.getString("album",null))).first()!!.title)
                if(verifyScheduler) {
                    assertTrue("Only cancel the job created by this fixture",fixture.getBoolean("ownsSyncJob",false))
                    assertPersistedSyncJob()
                    scheduler.cancel(162)
                }
                repo.deleteAlbum(requireNotNull(fixture.getString("album",null)));fixture.edit().clear().commit()
            }
            else -> error("Unknown staged check")
        }
        assertNull(access.preferences().getSession().getOrThrow());assertEquals(before,counter.count)
    }
}
