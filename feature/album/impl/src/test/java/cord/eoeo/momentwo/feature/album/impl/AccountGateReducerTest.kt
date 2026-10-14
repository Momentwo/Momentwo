package cord.eoeo.momentwo.feature.album.impl
import cord.eoeo.momentwo.feature.album.impl.account.*
import cord.eoeo.momentwo.core.model.AccountIdentity
import org.junit.Assert.*
import org.junit.Test
class AccountGateReducerTest {
    @Test fun accountSwitchCannotReusePreviousFeatureStateEvenWhenSwitchingBack(){
        val first=AccountGateReducer.reduce(AccountGateState(),AccountGateAction(AccountIdentity("A","one",true)));assertTrue(first.allowed)
        val next=AccountGateReducer.reduce(first,AccountGateAction(AccountIdentity("B","two",true)));assertFalse(next.allowed)
        assertFalse(AccountGateReducer.reduce(next,AccountGateAction(AccountIdentity("A","three",true))).allowed)
    }
    @Test fun expiryKeepsSameAccountCachedScreenWhileLogoutHidesIt(){
        val first=AccountGateReducer.reduce(AccountGateState(),AccountGateAction(AccountIdentity("A","one",true)))
        assertTrue(AccountGateReducer.reduce(first,AccountGateAction(AccountIdentity("A","one",false))).allowed)
        assertFalse(AccountGateReducer.reduce(first,AccountGateAction(null)).allowed)
    }
    @Test fun anEmptySuccessfulRefreshFinishesEvenWhenRoomDoesNotEmitNewRows(){
        val content=AccountLibraryContent(AccountIdentity("A","one",true),emptyList(),0,0,0)
        val before=AccountLibraryState(cord.eoeo.momentwo.core.common.ScreenState.Success(content))
        val loading=AccountLibraryReducer.reduce(before,AccountLibraryAction.Refreshing)
        assertEquals(cord.eoeo.momentwo.core.common.RefreshState.Loading,(loading.screen as cord.eoeo.momentwo.core.common.ScreenState.Success).refresh)
        assertEquals(before,AccountLibraryReducer.reduce(loading,AccountLibraryAction.Refreshed))
    }

}
