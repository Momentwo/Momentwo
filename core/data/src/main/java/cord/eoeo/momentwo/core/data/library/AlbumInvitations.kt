package cord.eoeo.momentwo.core.data.library
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
object AlbumInvitations {
    private val adapter = Moshi.Builder().build().adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
    fun encode(names: List<String>): String { require(names.all { it.isNotBlank() }); return adapter.toJson(names.distinct()) }
    fun decode(value: String): List<String> = if (value.isEmpty()) emptyList() else requireNotNull(adapter.fromJson(value)).also { require(it.all(String::isNotBlank)) }.distinct()
}
