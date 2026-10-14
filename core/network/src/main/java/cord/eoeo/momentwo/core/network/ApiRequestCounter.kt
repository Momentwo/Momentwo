package cord.eoeo.momentwo.core.network

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/** Counts API attempts without retaining URLs, headers, payloads or identity. */
@Singleton
class ApiRequestCounter @Inject constructor() {
    private val attempts = AtomicLong()
    val count: Long get() = attempts.get()
    fun record() { attempts.incrementAndGet() }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ApiDiagnostics { fun apiRequests(): ApiRequestCounter }
