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
internal class PhotoFakeSubAlbum : SubAlbumRepository {
    var rename: suspend () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun changeSubAlbumTitle(albumId: Int, subAlbumId: Int, title: String) = rename()
    override suspend fun requestCreateSubAlbum(albumId: Int, title: String) = Result.success(Unit)
    override suspend fun getSubAlbumList(albumId: Int) = Result.success(emptyList<SubAlbumItem>())
    override suspend fun deleteSubAlbums(albumId: Int, subAlbumIds: List<Int>) = Result.success(Unit)
}
