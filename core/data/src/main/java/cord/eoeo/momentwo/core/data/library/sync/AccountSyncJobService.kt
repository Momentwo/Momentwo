package cord.eoeo.momentwo.core.data.library.sync
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import cord.eoeo.momentwo.core.common.runSuspendCatching
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
@AndroidEntryPoint
class AccountSyncJobService:JobService(){
    @Inject internal lateinit var engine:SyncEngine
    @Inject lateinit var clients:AccountClientFactory
    @Inject lateinit var db:MomentwoDatabase
    @Inject lateinit var media:LocalMediaStore
    @Inject lateinit var scheduling:PlatformSyncScheduling
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private var running:Pair<String,Job>?=null
    override fun onStartJob(params:JobParameters):Boolean {
        val token=UUID.randomUUID().toString();scheduling.started(token)
        val job=scope.launch(start=CoroutineStart.LAZY){
            val result=runSuspendCatching{withContext(Dispatchers.IO){val session=clients.snapshot();if(!session.identity.canSync)false else engine.run(session.identity.accountId,AccountSyncTransport(session,db,clients,media)).also{runSuspendCatching{media.cleanUnused()}.onFailure{Log.e("AccountSync","Completed work; cleanup deferred",it)}}}}
            if(running?.first==token && scheduling.stopped(token)){running=null;jobFinished(params,result.getOrElse{Log.e("AccountSync","Stored work could not run",it);false})}
        }
        running=token to job;job.start();return true
    }
    override fun onStopJob(params:JobParameters):Boolean {val current=running ?: return false;running=null;scheduling.stopped(current.first);current.second.cancel();return true}
    override fun onDestroy(){scope.cancel();super.onDestroy()}
}
