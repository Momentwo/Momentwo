package cord.eoeo.momentwo.domain.album

import cord.eoeo.momentwo.core.model.AlbumItem

class GetAlbumListUseCase(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(): Result<List<AlbumItem>> = albumRepository.getAlbumList()
}
