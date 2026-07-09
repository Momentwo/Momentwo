package cord.eoeo.momentwo.domain.photo
import cord.eoeo.momentwo.core.data.photo.PhotoRepository

class UpdateIsLikedUseCase(private val photoRepository: PhotoRepository) {
    suspend operator fun invoke(photoId: Int, isLiked: Boolean) =
        photoRepository.updateIsLiked(photoId, isLiked)
}
