package cord.eoeo.momentwo.domain.friend

import cord.eoeo.momentwo.ui.model.FriendItem

class GetFriendListUseCase(
    private val friendRepository: FriendRepository,
) {
    suspend operator fun invoke(): Result<List<FriendItem>> = friendRepository.getFriendList()
}
