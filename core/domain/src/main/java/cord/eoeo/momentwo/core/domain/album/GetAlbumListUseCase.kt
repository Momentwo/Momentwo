package cord.eoeo.momentwo.core.domain.album
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.album.AlbumRepository

import cord.eoeo.momentwo.core.model.AlbumItem

class GetAlbumListUseCase @Inject constructor(
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(): Result<List<AlbumItem>> = albumRepository.getAlbumList()
}
