package cord.eoeo.momentwo.core.data.library.sync
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import cord.eoeo.momentwo.core.data.library.SyncScheduling
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class PlatformSyncScheduling @Inject constructor(@ApplicationContext private val context:Context):SyncScheduling {
    private val main=Handler(Looper.getMainLooper())
    private var running:String?=null
    private var dirty=false
    override fun enqueue(){main.post{if(running!=null)dirty=true else schedule()}}
    private fun schedule(force:Boolean=false){
        val manager=context.getSystemService(JobScheduler::class.java)
        if(!force && manager.getPendingJob(162)!=null)return
        val info=JobInfo.Builder(162,ComponentName(context,AccountSyncJobService::class.java)).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setBackoffCriteria(30_000,JobInfo.BACKOFF_POLICY_EXPONENTIAL).build()
        if(manager.schedule(info)!=JobScheduler.RESULT_SUCCESS)Log.e("AccountSync","Work remains stored; scheduling declined")
    }
    internal fun started(token:String){running=token;dirty=false}
    internal fun stopped(token:String):Boolean {
        if(running!=token)return false
        running=null
        if(dirty){dirty=false;main.post{main.post{if(running==null)schedule(true)else dirty=true}}}
        return true
    }
}
