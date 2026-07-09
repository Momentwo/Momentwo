package cord.eoeo.momentwo.core.domain.photo
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.photo.PhotoRepository

class DownloadPhotoUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend operator fun invoke(imageUrl: String): Result<Unit> =
        photoRepository.downloadPhoto(imageUrl)
}
