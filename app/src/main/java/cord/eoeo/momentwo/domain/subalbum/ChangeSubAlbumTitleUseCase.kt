package cord.eoeo.momentwo.domain.subalbum

class ChangeSubAlbumTitleUseCase(
    private val subAlbumRepository: SubAlbumRepository,
) {
    suspend operator fun invoke(
        albumId: Int,
        subAlbumId: Int,
        title: String,
    ): Result<Unit> = subAlbumRepository.changeSubAlbumTitle(albumId, subAlbumId, title)
}
