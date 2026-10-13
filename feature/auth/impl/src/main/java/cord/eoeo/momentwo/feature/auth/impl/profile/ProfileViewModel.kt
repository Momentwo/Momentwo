package cord.eoeo.momentwo.feature.auth.impl.profile

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.ReducerViewModel
import cord.eoeo.momentwo.feature.auth.api.ProfileDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
    ReducerViewModel<ProfileState, ProfileEvent, Nothing, ProfileEffect>(
        ProfileState(savedStateHandle.toRoute<ProfileDestination>().nickname), ProfileReducer,
    ) {
    override fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.OnError -> sendEffect(ProfileEffect.ShowSnackbar(event.errorMessage))
        }
    }
}
