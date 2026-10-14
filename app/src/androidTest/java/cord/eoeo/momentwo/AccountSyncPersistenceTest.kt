package cord.eoeo.momentwo

import android.content.ContextWrapper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.datastore.PreferenceRepositoryImpl
import cord.eoeo.momentwo.core.model.*
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountSyncPersistenceTest {
    @get:Rule val migrations=MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),MomentwoDatabase::class.java)
    @Test fun v3UpgradePreservesDeviceDataAndOnlyLinksOwnedAccountRows() {
        val name="sync-migration-${UUID.randomUUID()}.db"
        migrations.createDatabase(name,3).apply {
            execSQL("INSERT INTO library VALUES ('device',NULL,'이 기기'),('account:A','A','A')")
            execSQL("INSERT INTO local_album(localId,libraryId,title,subtitle,revision,createdAt) VALUES ('local','device','original','note',4,1)")
            execSQL("INSERT INTO local_album(localId,libraryId,title,subtitle,remoteId,revision,createdAt) VALUES ('remote','account:A','server','',7,0,1)")
            execSQL("INSERT INTO local_subalbum(localId,albumId,title,revision,createdAt) VALUES ('child','local','sub',0,1)")
            execSQL("INSERT INTO local_photo(localId,subAlbumId,mediaName,remoteUrl,description,descriptionKnown,favorite,isLiked,revision,createdAt) VALUES ('photo','child','owned.png','','private',1,1,0,2,1)")
            execSQL("INSERT INTO local_subalbum(localId,albumId,title,revision,createdAt) VALUES ('account-sub','remote','server sub',0,1)")
            execSQL("INSERT INTO local_photo(localId,subAlbumId,remoteUrl,description,descriptionKnown,favorite,isLiked,revision,createdAt) VALUES ('account-photo','account-sub','url','',0,0,0,0,1)")
            execSQL("INSERT INTO local_memo VALUES ('memo','photo','only me',1)");close()
        }
        migrations.runMigrationsAndValidate(name,4,true,MIGRATION_3_4).apply {
            query("SELECT description,favorite,mediaName FROM local_photo WHERE localId='photo'").use{assertTrue(it.moveToFirst());assertEquals("private",it.getString(0));assertEquals(1,it.getInt(1));assertEquals("owned.png",it.getString(2))}
            query("SELECT accountId,remoteId FROM sync_link WHERE localId='remote'").use{assertEquals(1,it.count);it.moveToFirst();assertEquals("A",it.getString(0));assertEquals(7,it.getInt(1))}
            query("SELECT commentsLoaded FROM local_photo WHERE localId='account-photo'").use{assertTrue(it.moveToFirst());assertEquals(0,it.getInt(0))}
            query("SELECT COUNT(*) FROM local_memo").use{it.moveToFirst();assertEquals(1,it.getInt(0))};close()
        }
    }
    @Test fun accountCreationPersistsInvitationsAndRejectsChangedAccount()=fixture { db,_,sync,prefs ->
        val expected=prefs.getSession().getOrThrow()!!.identity
        sync.createAccountAlbum("Invited","invite-album",expected,listOf("친구", "a,b"))
        val job=db.syncDao().jobs("A").single()
        assertEquals(listOf("친구", "a,b"),AlbumInvitations.decode(job.extra))
        prefs.storeSession("B","b","r")
        val result=cord.eoeo.momentwo.core.common.runSuspendCatching { sync.createAccountAlbum("Wrong","wrong-album",expected,listOf("친구")) }
        assertTrue(result.isFailure);assertNull(db.libraryDao().album("wrong-album"))
    }
    @Test fun offlineMutationsCoalesceAndCancellationPreservesIndependentFavorite()=fixture { db,repo,sync,prefs ->
        val library="account:A";db.libraryDao().insertLibrary(LibraryEntity(library,"A","A"))
        db.libraryDao().insertAlbum(LocalAlbumEntity("album",library,"Remote",remoteId=1,role=MemberAuth.ADMIN.roleString,createdAt=0))
        db.libraryDao().insertSubAlbum(LocalSubAlbumEntity("sub","album","Sub",remoteId=2,createdAt=0))
        db.libraryDao().insertPhoto(LocalPhotoEntity("photo","sub",null,remoteId=3,description="base",createdAt=0))
        db.syncDao().putLink(SyncLinkEntity("photo","A","PHOTO","sub",3))
        repo.setDescription("photo","first");repo.setDescription("photo","latest");repo.setFavorite("photo",true)
        val job=db.syncDao().jobs("A").single();assertEquals("base",job.baseText);assertEquals("latest",job.text)
        db.syncDao().updateJob(job.copy(createdAt=System.currentTimeMillis()+86400000L))
        sync.cancel(job.id,prefs.getSession().getOrThrow()!!.identity)
        val photo=repo.photo("photo").first()!!;assertEquals("base",photo.description);assertTrue(photo.favorite)
        assertEquals("DONE",db.syncDao().job(job.id)!!.status)
        repo.deletePhoto("photo");assertNull(repo.photo("photo").first())
        val deletion=db.syncDao().jobs("A").last{it.kind=="DELETE_PHOTO"};assertTrue(deletion.createdAt>db.syncDao().job(job.id)!!.createdAt)
        sync.cancel(deletion.id,prefs.getSession().getOrThrow()!!.identity)
        assertEquals("base",repo.photo("photo").first()!!.description);assertTrue(repo.photo("photo").first()!!.favorite)
        repo.deleteAlbum("album");assertNull(repo.album("album").first())
        sync.cancel(db.syncDao().jobs("A").last{it.kind=="DELETE_ALBUM"}.id,prefs.getSession().getOrThrow()!!.identity)
        assertNotNull(repo.album("album").first());assertNotNull(repo.photo("photo").first())
        val draft=sync.createAccountAlbum("Unsent","draft")
        val child=repo.createSubAlbum(draft,"New","new-sub")
        repo.deleteAlbum(draft)
        assertNull(db.libraryDao().album(draft));assertNull(db.libraryDao().subAlbum(child))
        assertTrue(db.syncDao().jobs("A").filter{it.targetId in setOf(draft,child)}.all{it.status=="CANCELLED"})
        prefs.storeSession("B","b","r");assertNull(repo.photo("photo").first());assertTrue(sync.observeWorks().first().isEmpty())
        assertNotNull(db.libraryDao().photo("photo"))
    }
    @Test fun replacingQueuedCoverDiscardsPreviouslyStagedObject()=fixture { db,repo,_,_ ->
        db.libraryDao().insertLibrary(LibraryEntity("account:A","A","A"))
        db.libraryDao().insertAlbum(LocalAlbumEntity("album","account:A","Cover",remoteId=1,role=MemberAuth.ADMIN.roleString,createdAt=0))
        val source=File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"cover-${UUID.randomUUID()}.png")
        try {
            android.graphics.Bitmap.createBitmap(8,8,android.graphics.Bitmap.Config.ARGB_8888).also{bitmap->source.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
            repo.setCover("album",android.net.Uri.fromFile(source))
            val job=db.syncDao().jobs("A").single();db.syncDao().updateJob(job.copy(transferPhase=2,objectKey="previous-object"))
            repo.setCover("album",android.net.Uri.fromFile(source))
            val updated=db.syncDao().jobs("A").single();assertEquals(job.id,updated.id);assertNotEquals(job.mediaName,updated.mediaName);assertEquals(0,updated.transferPhase);assertNull(updated.objectKey)
            repo.setCover("album",null);assertEquals("",repo.album("album").first()!!.coverUri)
        } finally {source.delete()}
    }
    @Test fun cancellingCopyNeverDeletesOriginalOrUploadsPrivateNotes()=fixture { db,repo,sync,prefs ->
        val original=repo.createAlbum("Device");val child=repo.createSubAlbum(original,"Child")
        sync.copyAlbums(setOf(original),prefs.getSession().getOrThrow()!!.identity,"copy")
        val replica=repo.albums(LibraryScope.Account("A")).first().single().localId
        val job=db.syncDao().jobs("A").first()
        sync.cancel(job.id,prefs.getSession().getOrThrow()!!.identity)
        assertNotNull(repo.album(original).first());assertNotNull(repo.subAlbum(child).first());assertNull(db.libraryDao().album(replica))
        assertTrue(db.syncDao().jobs("A").all{it.status=="CANCELLED"})
    }
    @Test fun interruptedAttemptsRecoverWithoutReplayingUncertainWritesAndOldClaimsCannotSettle()=fixture { db,_,_,_ ->
        fun job(id:String,status:SyncStatus,kind:SyncKind=SyncKind.CREATE_ALBUM,phase:Int=0,owner:String="A")=SyncJobEntity(id,owner,null,id,null,kind.name,status.name,id,mediaName=if(kind==SyncKind.UPLOAD_PHOTO)"owned.png"else null,baseText=null,baseExtra=null,remoteText=null,remoteExtra=null,dependency=null,revision=0,createdAt=0,claimId="old",transferPhase=phase)
        val prepared=job("prepared",SyncStatus.PREPARING);val sent=job("sent",SyncStatus.SENDING)
        val staged=job("staged",SyncStatus.SENDING,SyncKind.UPLOAD_PHOTO,1);val record=job("record",SyncStatus.SENDING,SyncKind.UPLOAD_PHOTO,2)
        val other=job("other",SyncStatus.SENDING,owner="B")
        listOf(prepared,sent,staged,record,other).forEach{db.syncDao().insertJob(it)}
        SyncTestHarness.recover(db,"A")
        assertEquals("QUEUED",db.syncDao().job("prepared")!!.status);assertEquals("UNKNOWN",db.syncDao().job("sent")!!.status)
        assertEquals("QUEUED",db.syncDao().job("staged")!!.status);assertEquals("UNKNOWN",db.syncDao().job("record")!!.status)
        assertEquals("SENDING",db.syncDao().job("other")!!.status)
        assertTrue(SyncTestHarness.claim(db,prepared,"new"));assertFalse(SyncTestHarness.claim(db,prepared,"duplicate"))
        SyncTestHarness.settle(db,prepared,"old",SyncStatus.DONE)
        assertEquals("PREPARING",db.syncDao().job("prepared")!!.status)
        assertFalse(SyncTestHarness.sending(db,prepared,"old"));assertTrue(SyncTestHarness.sending(db,prepared,"new"))
        SyncTestHarness.settle(db,prepared,"new",SyncStatus.DONE);assertEquals("DONE",db.syncDao().job("prepared")!!.status)
    }
    private fun fixture(block:suspend(MomentwoDatabase,RoomLocalLibraryRepository,AccountSyncRepository,PreferenceRepositoryImpl)->Unit)=runBlocking {
        val target=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(target.filesDir,"sync-test-${UUID.randomUUID()}").apply{mkdirs()}
        val context=object:ContextWrapper(target){override fun getFilesDir()=root}
        val db=Room.inMemoryDatabaseBuilder(context,MomentwoDatabase::class.java).build()
        val values=MutableStateFlow<Preferences>(emptyPreferences())
        val prefs=PreferenceRepositoryImpl(object:DataStore<Preferences>{override val data:Flow<Preferences> = values;override suspend fun updateData(transform:suspend(Preferences)->Preferences)=transform(values.value).also{values.value=it}})
        prefs.storeSession("A","a","r")
        val media=LocalMediaStore(context,db);val sync=AccountSyncRepository(db,prefs,media,object:SyncScheduling{override fun enqueue(){}})
        try{block(db,RoomLocalLibraryRepository(db,context,media,prefs,sync),sync,prefs)}finally{db.close();root.deleteRecursively()}
    }
}
