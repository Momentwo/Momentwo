package cord.eoeo.momentwo.core.data.library

/** Kept injectable so durable snapshot tests never submit work to a real backend. */
interface SyncScheduling { fun enqueue() }
