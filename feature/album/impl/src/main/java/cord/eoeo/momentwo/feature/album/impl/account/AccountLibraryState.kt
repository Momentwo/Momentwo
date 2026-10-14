package cord.eoeo.momentwo.feature.album.impl.account
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class AccountLibraryContent(val identity:AccountIdentity?,val albums:List<LocalAlbum>,val deviceAlbums:Int,val usedBytes:Long,val availableBytes:Long)
internal data class AccountLibraryState(val screen:ScreenState<AccountLibraryContent> = ScreenState.Loading,val pending:Set<String> = emptySet()):UiState
