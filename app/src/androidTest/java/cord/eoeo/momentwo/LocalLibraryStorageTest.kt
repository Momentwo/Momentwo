package cord.eoeo.momentwo

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.RoomLocalLibraryRepository
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.model.LibraryScope
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalLibraryStorageTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @get:Rule val migration = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), MomentwoDatabase::class.java)

    @Test fun migrationPreservesIdentityAndDoesNotAdoptUnownedRemoteCache() {
        val name = "library-migration-${UUID.randomUUID()}"
        migration.createDatabase(name, 1).apply {
            execSQL("INSERT INTO photo VALUES (1, 2, 3, 'remote', 0)")
            execSQL("INSERT INTO profile VALUES ('identity', 'name', 'nickname', 'phone', 'image')")
            close()
        }
        try {
            migration.runMigrationsAndValidate(name, 2, true, MIGRATION_1_2).use { db ->
                db.query("SELECT COUNT(*) FROM photo").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
                db.query("SELECT COUNT(*) FROM local_album").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
                db.query("SELECT id FROM library").use { it.moveToFirst(); assertEquals("device", it.getString(0)) }
                db.query("SELECT username FROM profile").use { it.moveToFirst(); assertEquals("identity", it.getString(0)) }
            }
        } finally { context.deleteDatabase(name) }
    }

    @Test fun localTreeSurvivesReopenAndDeletingParentCascadesOnlyItsChildren() = runBlocking {
        val name = "library-persistence-${UUID.randomUUID()}"
        fun open() = Room.databaseBuilder(context, MomentwoDatabase::class.java, name).addMigrations(MIGRATION_1_2).build()
        var db = open()
        try {
            var repository = RoomLocalLibraryRepository(db, context)
            repository.initialize(); repository.initialize()
            val album = repository.createAlbum("Owned")
            val sibling = repository.createAlbum("Other")
            val sub = repository.createSubAlbum(album, "Child")
            db.close(); db = open(); repository = RoomLocalLibraryRepository(db, context)
            assertEquals(setOf(album, sibling), repository.albums(LibraryScope.Device).first().map { it.localId }.toSet())
            assertEquals(sub, repository.subAlbums(album).first().single().localId)
            repository.deleteAlbum(album)
            assertNull(repository.subAlbum(sub).first())
            assertEquals(sibling, repository.albums(LibraryScope.Device).first().single().localId)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun localCrudCannotMutateAnotherLibrary() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, MomentwoDatabase::class.java).build()
        try {
            db.libraryDao().insertLibrary(LibraryEntity("account:test", "test", "Account"))
            db.libraryDao().insertAlbum(LocalAlbumEntity("foreign", "account:test", "Original", createdAt = 0))
            val repository = RoomLocalLibraryRepository(db, context)
            try { repository.editAlbum("foreign", "Changed", ""); fail("Expected isolation") } catch (_: IllegalStateException) { }
            assertEquals("Original", db.libraryDao().album("foreign")!!.title)
        } finally { db.close() }
    }
}
