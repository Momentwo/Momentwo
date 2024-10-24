package cord.eoeo.momentwo.data.presigned.remote

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Url

interface PresignedService {
    @Headers("isAuthorizable: false")
    @PUT
    suspend fun uploadImage(
        @Url presignedUrl: String,
        @Body file: RequestBody,
    )
}
