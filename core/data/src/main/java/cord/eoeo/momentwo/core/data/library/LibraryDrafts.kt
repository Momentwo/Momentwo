package cord.eoeo.momentwo.core.data.library

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import cord.eoeo.momentwo.core.model.LibraryDraft
import javax.inject.Inject
import kotlinx.coroutines.flow.first

interface LibraryDrafts {
    suspend fun load(key: String): LibraryDraft?
    suspend fun save(key: String, draft: LibraryDraft)
    suspend fun clearIfMatches(key: String, token: String)
}
class PreferenceLibraryDrafts @Inject constructor(private val store: DataStore<Preferences>) : LibraryDrafts {
    private fun prefKey(name: String, field: String) = stringPreferencesKey("library.draft.$name.$field")
    override suspend fun load(key: String): LibraryDraft? {
        val values = store.data.first()
        val token = values[prefKey(key, "token")] ?: return null
        return LibraryDraft(values[prefKey(key, "text")].orEmpty(), values[prefKey(key, "extra")].orEmpty(), token, values[prefKey(key, "context")].orEmpty())
    }
    override suspend fun save(key: String, draft: LibraryDraft) {
        store.edit { it[prefKey(key, "text")] = draft.text; it[prefKey(key, "extra")] = draft.extra; it[prefKey(key, "token")] = draft.token; it[prefKey(key, "context")] = draft.context }
    }
    override suspend fun clearIfMatches(key: String, token: String) {
        store.edit { if (it[prefKey(key, "token")] == token) listOf("text", "extra", "token", "context").forEach { field -> it.remove(prefKey(key, field)) } }
    }
}
