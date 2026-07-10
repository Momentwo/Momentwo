package cord.eoeo.momentwo.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.data.profile.ProfileRepository
import cord.eoeo.momentwo.core.common.BaseViewModel
import cord.eoeo.momentwo.ui.MomentwoDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        savedStateHandle: SavedStateHandle,
    ) : BaseViewModel<ProfileContract.State, ProfileContract.Event, ProfileContract.Effect>() {
        init {
            val (nickname) = savedStateHandle.toRoute<MomentwoDestination.Profile>()
            setState(uiState.value.copy(nickname = nickname))
        }

        override fun createInitialState(): ProfileContract.State = ProfileContract.State()

        override fun handleEvent(newEvent: ProfileContract.Event) {
            when (newEvent) {
                is ProfileContract.Event.OnError -> {
                    setEffect { ProfileContract.Effect.ShowSnackbar(newEvent.errorMessage) }
                }
            }
        }
    }
