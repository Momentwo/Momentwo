package cord.eoeo.momentwo.core.data.library.sync
import android.util.Log
import androidx.room.withTransaction
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.SyncScheduling
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.model.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@Singleton
class SyncBootstrap @Inject constructor(private val db:MomentwoDatabase,private val preferences:PreferenceRepository,private val scheduler:SyncScheduling):BackgroundTasksInitializer {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private var started=false
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start(){if(started)return;started=true;scope.launch{
        runSuspendCatching {
            preferences.identities().flatMapLatest { identity ->
                if(identity?.canSync != true) flowOf(emptyList<String>()) else {
                    db.withTransaction{db.syncDao().jobs(identity.accountId).filter{it.status==SyncStatus.AUTH_REQUIRED.name}.forEach{db.syncDao().updateJob(it.copy(status=SyncStatus.QUEUED.name))}}
                    db.syncDao().observeJobs(identity.accountId).map { jobs -> jobs.filter { it.status in setOf(SyncStatus.PREPARING.name,SyncStatus.SENDING.name) || (it.status==SyncStatus.QUEUED.name && (it.dependency==null || jobs.any { parent->parent.id==it.dependency && parent.status==SyncStatus.DONE.name })) }.map { it.id } }
                }
            }.distinctUntilChanged().collect { eligible -> if(eligible.isNotEmpty())scheduler.enqueue() }

        }.onFailure{Log.e("AccountSync","Stored work will resume from account screen",it)}
    }}
}
