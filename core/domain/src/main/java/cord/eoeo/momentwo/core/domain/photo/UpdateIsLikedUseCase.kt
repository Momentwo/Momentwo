package cord.eoeo.momentwo.core.domain.photo
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.photo.PhotoRepository

class UpdateIsLikedUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend operator fun invoke(photoId: Int, isLiked: Boolean) =
        photoRepository.updateIsLiked(photoId, isLiked)
}
