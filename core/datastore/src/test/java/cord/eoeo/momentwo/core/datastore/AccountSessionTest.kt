package cord.eoeo.momentwo.core.datastore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
class AccountSessionTest {
    private class Store:DataStore<Preferences>{val values=MutableStateFlow<Preferences>(emptyPreferences());var writes=0;private val lock=Mutex();override val data:Flow<Preferences> = values;override suspend fun updateData(transform:suspend(Preferences)->Preferences)=lock.withLock {transform(values.value).also{writes++;values.value=it}}}
    @Test fun ownerAndCredentialsBecomeVisibleInOneWriteAndRemainRedacted()=runTest{
        val store=Store();val repository=PreferenceRepositoryImpl(store)
        repository.storeSession("canonical","access","refresh")
        val session=repository.getSession().getOrThrow()!!
        assertEquals(1,store.writes);assertEquals("canonical",session.identity.accountId);assertEquals("access",session.accessToken);assertEquals("refresh",session.refreshToken)
        assertFalse(session.toString().contains("access"));assertFalse(session.toString().contains("refresh"))
    }
    @Test fun staleExpiryCannotExpireAnotherLoginEvenToTheSameAccount()=runTest{
        val repository=PreferenceRepositoryImpl(Store());repository.storeSession("A","a1","r1");val old=repository.getSession().getOrThrow()!!
        repository.storeSession("B","b","rb");repository.expireSession("A",old.identity.epoch);assertTrue(repository.getSession().getOrThrow()!!.identity.canSync)
        repository.storeSession("A","a2","r2");repository.expireSession("A",old.identity.epoch);assertTrue(repository.getSession().getOrThrow()!!.identity.canSync)
        val latest=repository.getSession().getOrThrow()!!;repository.expireSession("A",latest.identity.epoch);assertFalse(repository.getSession().getOrThrow()!!.identity.canSync)
    }
    @Test fun logoutClearsOnlySessionAndUnownedOldTokensCannotBecomeAnAccount()=runTest{
        val store=Store();val draft=stringPreferencesKey("library.draft.device.text");store.updateData{it.toMutablePreferences().apply{this[draft]="private"}}
        val repository=PreferenceRepositoryImpl(store);repository.storeAccessToken("legacy");assertNull(repository.getSession().getOrThrow())
        repository.storeSession("A","a","r");repository.clearSession();assertNull(repository.identities().first());assertEquals("",repository.getAccessToken().getOrThrow());assertEquals("private",store.values.value[draft])
    }
}
