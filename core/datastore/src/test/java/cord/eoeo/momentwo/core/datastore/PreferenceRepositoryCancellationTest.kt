package cord.eoeo.momentwo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PreferenceRepositoryCancellationTest {
    @Test fun cancelledReadCannotBecomeEmptyTokenOrFailure() = runTest {
        val entered = CompletableDeferred<Unit>()
        val store = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow {
                entered.complete(Unit)
                awaitCancellation()
            }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                error("No write expected")
        }
        var returned = false
        val read = launch {
            PreferenceRepositoryImpl(store).getAccessToken()
            returned = true
        }
        entered.await()
        read.cancelAndJoin()
        assertFalse(returned)
        assertTrue(read.isCancelled)
    }
}
