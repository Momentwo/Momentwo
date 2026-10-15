package cord.eoeo.momentwo.feature.photo.impl

import android.net.Uri
import androidx.paging.PagingData
import cord.eoeo.momentwo.core.data.photo.PhotoRepository
import cord.eoeo.momentwo.core.data.description.DescriptionRepository
import cord.eoeo.momentwo.core.data.like.LikeRepository
import cord.eoeo.momentwo.core.data.comment.CommentRepository
import cord.eoeo.momentwo.core.data.subalbum.SubAlbumRepository
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal val fixtureDescription = DescriptionItem("fixture", "", "title\nbody", "today", emptyList())
internal class PhotoFakeRepository : PhotoRepository {
    var delete: suspend () -> Result<Unit> = { Result.success(Unit) }
    var update: suspend () -> Unit = {}
    val deleted = mutableListOf<List<Int>>()
    val updated = mutableListOf<Pair<Int, Boolean>>()
    var source: suspend () -> Flow<PagingData<PhotoItem>> = { flowOf(PagingData.empty()) }
    var download: suspend () -> Result<Unit> = { Result.success(Unit) }
    var upload: suspend (Uri) -> Result<Unit> = { Result.success(Unit) }
    var sourceCalls = 0
    override suspend fun getPhotoPagingData(pageSize: Int, albumId: Int, subAlbumId: Int): Flow<PagingData<PhotoItem>> { sourceCalls++; return source() }
    override suspend fun requestUploadPhoto(albumId: Int, subAlbumId: Int, image: Uri) = upload(image)
    override suspend fun deletePhotos(albumId: Int, subAlbumId: Int, photoIds: List<Int>): Result<Unit> { deleted += photoIds.toList(); return delete() }
    override suspend fun updateIsLiked(photoId: Int, isLiked: Boolean) { updated += photoId to isLiked; update() }
    override suspend fun downloadPhoto(imageUrl: String) = download()
}
internal class PhotoFakeDescription : DescriptionRepository {
    var description = fixtureDescription
    var read: suspend () -> Result<DescriptionItem> = { Result.success(description) }
    var write: suspend () -> Result<Unit> = { Result.success(Unit) }
    val writes = mutableListOf<String>()
    override suspend fun getDescription(albumId: Int, photoId: Int) = read()
    override suspend fun writeDescription(albumId: Int, photoId: Int, description: String): Result<Unit> {
        writes += description
        return write().onSuccess { this.description = this.description.copy(description = description) }
    }
    override suspend fun editDescription(albumId: Int, photoId: Int, description: String) = writeDescription(albumId, photoId, description)
    override suspend fun deleteDescription(albumId: Int, photoId: Int) = writeDescription(albumId,photoId,"")
}
internal class PhotoFakeLike : LikeRepository {
    var count = 10
    var read: suspend () -> Result<Int> = { Result.success(count) }
    var write: suspend () -> Result<Unit> = { Result.success(Unit) }
    var calls = 0
    override suspend fun getLikeCount(albumId: Int, photoId: Int) = read()
    override suspend fun requestDoLike(albumId: Int, photoId: Int): Result<Unit> { calls++; return write().onSuccess { count++ } }
    override suspend fun requestUndoLike(albumId: Int, photoId: Int): Result<Unit> { calls++; return write().onSuccess { count-- } }
}
internal class PhotoFakeComment : CommentRepository {
    var write: suspend () -> Result<Unit> = { Result.success(Unit) }
    var edit: suspend () -> Result<Unit> = { Result.success(Unit) }
    var sourceCalls = 0
    val writes = mutableListOf<String>()
    override suspend fun getCommentPagingData(albumId: Int, photoId: Int, pageSize: Int): Flow<PagingData<CommentItem>> { sourceCalls++; return flowOf(PagingData.empty()) }
    override suspend fun writeComment(albumId: Int, photoId: Int, comments: String): Result<Unit> { writes += comments; return write() }
    override suspend fun editComment(albumId: Int, commentId: Int, comments: String) = edit()
    override suspend fun deleteComment(albumId: Int, commentId: Int) = edit()
}
internal class PhotoFakeSubAlbum : SubAlbumRepository {
    var rename: suspend () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun changeSubAlbumTitle(albumId: Int, subAlbumId: Int, title: String) = rename()
    override suspend fun requestCreateSubAlbum(albumId: Int, title: String) = Result.success(Unit)
    override suspend fun getSubAlbumList(albumId: Int) = Result.success(emptyList<SubAlbumItem>())
    override suspend fun deleteSubAlbums(albumId: Int, subAlbumIds: List<Int>) = Result.success(Unit)
}
