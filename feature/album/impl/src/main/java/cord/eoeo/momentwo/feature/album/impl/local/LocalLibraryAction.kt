package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.model.LocalAlbum
internal sealed interface LocalLibraryAction { data class Loaded(val albums: List<LocalAlbum>): LocalLibraryAction;data object Failed: LocalLibraryAction }
