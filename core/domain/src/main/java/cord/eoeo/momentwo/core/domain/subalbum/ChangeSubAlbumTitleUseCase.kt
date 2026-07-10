package cord.eoeo.momentwo.core.domain.subalbum
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.subalbum.SubAlbumRepository

class ChangeSubAlbumTitleUseCase @Inject constructor(
    private val subAlbumRepository: SubAlbumRepository,
) {
    suspend operator fun invoke(
        albumId: Int,
        subAlbumId: Int,
        title: String,
    ): Result<Unit> = subAlbumRepository.changeSubAlbumTitle(albumId, subAlbumId, title)
}
