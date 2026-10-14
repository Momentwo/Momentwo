package cord.eoeo.momentwo.core.data.library
import cord.eoeo.momentwo.core.data.model.*
import cord.eoeo.momentwo.core.network.ApiModule
import org.junit.Assert.*
import org.junit.Test
class AccountPayloadTest {
    private val moshi = ApiModule.provideJsonParser()
    @Test fun albumWireNamesAndRequiredFieldsDecode() {
        val result = moshi.adapter(AlbumInfoList::class.java).fromJson("""{"albumInfoList":[{"id":1,"title":"title","subTitle":"subtitle","profileImage":"image","albumCount":2}]}""")!!
        assertEquals("title",result.albumList.single().title)
    }
    @Test fun subAlbumWireNamesDecode() {
        val result = moshi.adapter(SubAlbumList::class.java).fromJson("""{"subAlbumList":[{"id":2,"subAlbumTitle":"sub","subTitleImageList":[]}]}""")!!
        assertEquals("sub",result.subAlbumList.single().title)
    }
    @Test fun terminalPhotoPageKeepsDataAndNullableCursor() {
        val result = moshi.adapter(PhotoPage::class.java).fromJson("""{"images":[{"id":3,"imageUrl":"photo"}],"nextCursor":null}""")!!
        assertEquals(3,result.images.single().id);assertNull(result.nextCursor)
    }
}
