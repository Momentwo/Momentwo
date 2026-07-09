package cord.eoeo.momentwo.core.domain.friend
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.friend.FriendRepository

import cord.eoeo.momentwo.core.model.FriendItem

class GetFriendListUseCase @Inject constructor(
    private val friendRepository: FriendRepository,
) {
    suspend operator fun invoke(): Result<List<FriendItem>> = friendRepository.getFriendList()
}
