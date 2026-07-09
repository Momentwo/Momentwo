package cord.eoeo.momentwo.core.data.description.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.core.data.model.CreateDescription
import cord.eoeo.momentwo.core.data.model.Description
import cord.eoeo.momentwo.core.data.model.EditDescription
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DescriptionService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_CREATE_DESCRIPTION)
    suspend fun postCreateDescription(
        @Body createDescription: CreateDescription,
    )

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_EDIT_DESCRIPTION)
    suspend fun putEditDescription(
        @Body editDescription: EditDescription,
    )

    @DELETE(MomentwoApi.DELETE_DESCRIPTION)
    suspend fun deleteDescription(
        @Path("albumId") albumId: Int,
        @Path("photoId") photoId: Int,
    )

    @GET(MomentwoApi.GET_DESCRIPTION)
    suspend fun getDescription(
        @Path("albumId") albumId: Int,
        @Path("photoId") photoId: Int,
    ): Description
}
