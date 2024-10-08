package cord.eoeo.momentwo.data.album

import android.net.Uri
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.domain.album.AlbumRepository
import cord.eoeo.momentwo.ui.model.AlbumItem
import cord.eoeo.momentwo.ui.model.MemberAuth

class AlbumRepositoryImpl(
    private val albumRemoteDataSource: AlbumDataSource,
) : AlbumRepository {
    override suspend fun requestCreateAlbum(
        title: String,
        inviteList: List<String>,
    ): Result<Unit> = albumRemoteDataSource.requestCreateAlbum(CreateAlbumInfo(title, inviteList))

    override suspend fun deleteAlbum(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbum(albumId)

    override suspend fun changeAlbumImage(
        albumId: Int,
        profileImage: Uri,
    ): Result<Unit> {
        // TODO Uri to MultipartBody.Part
        return Result.success(Unit)
        // return albumRemoteDataSource.changeAlbumImage(AlbumImage(albumId, profileImage))
    }

    override suspend fun deleteAlbumImage(albumId: Int): Result<Unit> = albumRemoteDataSource.deleteAlbumImage(albumId)

    override suspend fun changeAlbumSubTitle(
        albumId: Int,
        subTitle: String,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumSubTitle(AlbumSubTitle(albumId, subTitle))

    override suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit> =
        albumRemoteDataSource.deleteAlbumSubTitle(albumId)

    override suspend fun changeAlbumTitle(
        albumId: Int,
        title: String,
    ): Result<Unit> = albumRemoteDataSource.changeAlbumTitle(albumId, title)

    override suspend fun getAlbumList(): Result<List<AlbumItem>> =
        albumRemoteDataSource.getAlbumList().map { albumItemList ->
            albumItemList.albumList.map { it.mapToAlbumItem() }
        }
    /*Result.success(
        listOf(
            AlbumItem(
                id = 0,
                title = "album1",
                subTitle = "sub1",
                imageUrl = "https://images.unsplash.com/photo-1717652480757-555af691acbb?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            AlbumItem(
                id = 1,
                title = "album2",
                subTitle = "sub2",
                imageUrl = "https://images.unsplash.com/photo-1717652480757-555af691acbb?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            AlbumItem(
                id = 2,
                title = "album3",
                subTitle = "sub3",
                imageUrl = "https://images.unsplash.com/photo-1717652480757-555af691acbb?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
        )
    )*/

    override suspend fun getAlbumRole(albumId: Int): Result<MemberAuth> =
        albumRemoteDataSource.getAlbumRole(albumId).map { albumRole ->
            albumRole.rules.mapToMemberAuth()
        }
}
