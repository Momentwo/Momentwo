package cord.eoeo.momentwo.core.domain.album
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.album.AlbumRepository

class RequestCreateAlbumUseCase @Inject constructor(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(
        title: String,
        inviteList: List<String>,
    ) = albumRepository.requestCreateAlbum(title, inviteList)
}
