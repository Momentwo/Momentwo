package cord.eoeo.momentwo.data.photo.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.DeletePhotos
import cord.eoeo.momentwo.data.model.PhotoPage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface PhotoService {
    @Headers("content-type: application/json")
    @HTTP(method = "DELETE", path = MomentwoApi.DELETE_PHOTOS, hasBody = true)
    suspend fun deletePhotos(
        @Body deletePhotos: DeletePhotos
    )

    @GET(MomentwoApi.GET_PHOTO_PAGE)
    suspend fun getPhotoPage(
        @Path("albumId") albumId: Int,
        @Path("subAlbumId") subAlbumId: Int,
        @Query("cursor") cursor: Int,
    ): PhotoPage
}
