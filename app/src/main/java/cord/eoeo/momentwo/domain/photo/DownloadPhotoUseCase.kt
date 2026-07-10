package cord.eoeo.momentwo.domain.photo
import cord.eoeo.momentwo.core.data.photo.PhotoRepository

class DownloadPhotoUseCase(private val photoRepository: PhotoRepository) {
    suspend operator fun invoke(imageUrl: String): Result<Unit> =
        photoRepository.downloadPhoto(imageUrl)
}
