package cord.eoeo.momentwo.data.album.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumInfoList
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlbumService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_CREATE_ALBUM)
    suspend fun postCreateAlbum(
        @Body createAlbumInfo: CreateAlbumInfo,
    )

    @DELETE(MomentwoApi.DELETE_ALBUM)
    suspend fun deleteAlbum(
        @Path("id") albumId: Int,
    )

    @Multipart
    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_ALBUM_IMAGE)
    suspend fun putAlbumImage(
        @Body albumImage: AlbumImage,
    )

    @DELETE(MomentwoApi.DELETE_ALBUM_IMAGE)
    suspend fun deleteAlbumImage(
        @Path("albumId") albumId: Int,
    )

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_ALBUM_SUBTITLE)
    suspend fun putAlbumSubTitle(
        @Body albumSubTitle: AlbumSubTitle,
    )

    @DELETE(MomentwoApi.DELETE_ALBUM_SUBTITLE)
    suspend fun deleteAlbumSubTitle(
        @Path("albumId") albumId: Int,
    )

    @FormUrlEncoded
    @PUT(MomentwoApi.PUT_ALBUM_TITLE)
    suspend fun putAlbumTitle(
        @Path("id") albumId: Int,
        @Body editTitle: String,
    )

    @GET(MomentwoApi.GET_ALBUM_LIST)
    suspend fun getAlbumList(): AlbumInfoList
}
