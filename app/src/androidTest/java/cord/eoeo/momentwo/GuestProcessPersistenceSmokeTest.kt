package cord.eoeo.momentwo

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.LibraryTestAccess
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
        val access=EntryPointAccessors.fromApplication(context,LibraryTestAccess::class.java)
        val counter=EntryPointAccessors.fromApplication(context,ApiDiagnostics::class.java).apiRequests()
        access.preferences().clearSession();val before=counter.count
        val fixture=context.getSharedPreferences("process-persistence-fixture",Context.MODE_PRIVATE)
        val repo=access.repository()
        when(stage) {
            "seed" -> {
                val source=File(context.cacheDir,"process-source.png")
                Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888).also{bitmap->bitmap.eraseColor(android.graphics.Color.BLUE);source.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
                val album=repo.createAlbum("Process original");val sub=repo.createSubAlbum(album,"Child");val photo=repo.importPhoto(sub,source.toUri())
                repo.setDescription(photo,"persisted description");repo.setFavorite(photo,true);repo.saveMemo(photo,"persisted memo")
                assertTrue(fixture.edit().putString("album",album).putString("sub",sub).putString("photo",photo).commit());assertTrue(source.delete())
            }
            "verify" -> {
                context.cacheDir.listFiles()?.forEach{it.deleteRecursively()};repo.cleanUnusedMedia()
                val photo=repo.photo(requireNotNull(fixture.getString("photo",null))).first()!!
                assertEquals("persisted description",photo.description);assertTrue(photo.favorite)
                assertEquals("persisted memo",repo.memos(photo.localId).first().single().text)
                assertTrue(access.media().file(requireNotNull(photo.mediaName)).isFile)
                assertEquals("Process original",repo.album(requireNotNull(fixture.getString("album",null))).first()!!.title)
                repo.deleteAlbum(requireNotNull(fixture.getString("album",null)));fixture.edit().clear().commit()
            }
            else -> error("Unknown staged check")
        }
        assertNull(access.preferences().getSession().getOrThrow());assertEquals(before,counter.count)
    }
}
