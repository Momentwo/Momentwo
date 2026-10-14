package cord.eoeo.momentwo.core.model

sealed interface LibraryScope {
    val id: String
    data object Device : LibraryScope { override val id = "device" }
    data class Account(val accountId: String) : LibraryScope { override val id = "account:$accountId" }
}

data class LocalAlbum(val localId: String, val libraryId: String, val title: String, val subtitle: String, val coverUri: String, val remoteId: Int?, val revision: Long, val role: String? = null, val subsLoaded: Boolean = true)
data class LocalSubAlbum(val localId: String, val albumId: String, val title: String, val remoteId: Int?, val revision: Long, val photosLoaded: Boolean = true)
data class LocalPhoto(val localId: String, val subAlbumId: String, val imageUri: String, val mediaName: String?, val remoteId: Int?, val description: String, val descriptionKnown: Boolean, val favorite: Boolean, val isLiked: Boolean, val likeCount: Int?, val revision: Long)
data class LocalMemo(val localId: String, val photoId: String, val text: String, val createdAt: Long)

data class LibraryDraft(val text: String, val extra: String, val token: String, val context: String = "")
