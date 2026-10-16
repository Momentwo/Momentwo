package cord.eoeo.momentwo

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.decode.DataSource
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import cord.eoeo.momentwo.core.data.library.LibraryTestAccess
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import dagger.hilt.android.EntryPointAccessors
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the application's actual singleton and export path against loopback fixtures only. */
@RunWith(AndroidJUnit4::class)
class ImageLoaderCompatibilityTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun png(width: Int = 12, height: Int = 8): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return try {
            bitmap.eraseColor(android.graphics.Color.rgb(44, 116, 164))
            ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                output.toByteArray()
            }
        } finally { bitmap.recycle() }
    }
    private fun image(bytes: ByteArray, cacheControl: String) = MockResponse.Builder()
        .addHeader("Content-Type", "image/png")
        .addHeader("Cache-Control", cacheControl)
        .body(Buffer().write(bytes))
        .build()
    private fun request(url: String) = ImageRequest.Builder(context)
        .data(url)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .build()
    private fun apiCounter() = EntryPointAccessors.fromApplication(context, ApiDiagnostics::class.java).apiRequests()

    @Test fun noStoreResponsesAreFetchedAgainWithoutApiAuthentication() = runBlocking {
        val server = MockWebServer()
        server.enqueue(image(png(), "no-store"))
        server.enqueue(image(png(), "no-store"))
        server.start()
        val counter = apiCounter()
        val before = counter.count
        try {
            val url = server.url("/no-store-${UUID.randomUUID()}.png").toString()
            repeat(2) {
                val result = context.imageLoader.execute(request(url))
                assertTrue(result.toString(), result is SuccessResult)
                result as SuccessResult
                assertEquals(DataSource.NETWORK, result.dataSource)
                assertEquals(12, result.image.width)
                assertEquals(8, result.image.height)
                val recorded = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
                assertNull(recorded.headers["Authorization"])
                assertNull(recorded.headers["isAuthorizable"])
            }
            assertEquals(2, server.requestCount)
            assertEquals(before, counter.count)
        } finally { server.close() }
    }

    @Test fun cacheableResponseIsAvailableWhenNetworkLoadingIsDisabled() = runBlocking {
        val server = MockWebServer()
        server.enqueue(image(png(), "max-age=3600"))
        server.start()
        val loader = context.imageLoader
        var key: String? = null
        try {
            val url = server.url("/cacheable-${UUID.randomUUID()}.png").toString()
            val initial = loader.execute(request(url))
            assertTrue(initial.toString(), initial is SuccessResult)
            initial as SuccessResult
            key = initial.diskCacheKey
            val cached = loader.execute(request(url).newBuilder()
                .networkCachePolicy(CachePolicy.DISABLED).build())
            assertTrue(cached.toString(), cached is SuccessResult)
            cached as SuccessResult
            assertEquals(DataSource.DISK, cached.dataSource)
            assertEquals(12, cached.image.width)
            assertEquals(8, cached.image.height)
            assertEquals(1, server.requestCount)
        } finally {
            key?.let { loader.diskCache?.remove(it) }
            server.close()
        }
    }

    @Test fun galleryExportPreservesOriginalDimensionsBeyondDefaultBitmapLimit() = runBlocking {
        awaitSdkRuntimeNetwork()
        val access = EntryPointAccessors.fromApplication(context, LibraryTestAccess::class.java)
        val preferences = access.preferences()
        assertNull("The isolated fixture starts without an account", preferences.getSession().getOrThrow())
        val server = MockWebServer()
        server.enqueue(image(png(width = 5000, height = 3), "no-store"))
        server.start()
        val filename = "momentwo-export-fixture-${UUID.randomUUID()}"
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val arguments = arrayOf("$filename.png")
        val resolver = context.contentResolver
        val counter = apiCounter()
        val before = counter.count
        try {
            // The legacy account adapter needs an identity; expired credentials cannot sync.
            val account = "image-export-${UUID.randomUUID()}"
            preferences.storeSession(account, "image-fixture-access", "image-fixture-refresh")
            val identity = requireNotNull(preferences.getSession().getOrThrow()).identity
            preferences.expireSession(account, identity.epoch)
            access.photos().downloadPhoto(server.url("/$filename.png").toString()).getOrThrow()
            val uri = requireNotNull(resolver.query(collection, arrayOf(MediaStore.Images.Media._ID), selection, arguments, null)).use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                ContentUris.withAppendedId(collection, it.getLong(0))
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            requireNotNull(resolver.openInputStream(uri)).use { BitmapFactory.decodeStream(it, null, bounds) }
            assertEquals(5000, bounds.outWidth)
            assertEquals(3, bounds.outHeight)
            assertEquals(1, server.requestCount)
            assertNull(requireNotNull(server.takeRequest(5, TimeUnit.SECONDS)).headers["Authorization"])
            assertEquals(before, counter.count)
        } finally {
            try {
                preferences.clearSession()
            } finally {
                try { resolver.delete(collection, selection, arguments) } finally { server.close() }
            }
        }
    }
}
