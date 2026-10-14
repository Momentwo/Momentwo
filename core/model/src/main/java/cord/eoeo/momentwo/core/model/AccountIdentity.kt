package cord.eoeo.momentwo.core.model
/** Safe to expose in UI; credentials never belong in State or persisted work payloads. */
data class AccountIdentity(val accountId: String, val epoch: String, val canSync: Boolean)
