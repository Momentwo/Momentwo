package cord.eoeo.momentwo.domain.album
import cord.eoeo.momentwo.core.data.album.AlbumRepository

class RequestCreateAlbumUseCase(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(
        title: String,
        inviteList: List<String>,
    ) = albumRepository.requestCreateAlbum(title, inviteList)
}
