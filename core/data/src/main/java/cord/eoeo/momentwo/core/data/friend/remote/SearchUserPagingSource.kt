package cord.eoeo.momentwo.core.data.friend.remote

import cord.eoeo.momentwo.core.data.PagedPagingSource
import cord.eoeo.momentwo.core.data.friend.FriendDataSource
import cord.eoeo.momentwo.core.data.model.SearchUserInfo

class SearchUserPagingSource(
    private val friendRemoteDataSource: FriendDataSource.Remote,
    private val nickname: String,
    private val size: Int,
) : PagedPagingSource<SearchUserInfo>() {
    override suspend fun loadPage(page: Int): LoadResult<Int, SearchUserInfo> {
        friendRemoteDataSource
            .getSearchUsers(nickname, page, size)
            .onSuccess { searchUserPage ->
                return LoadResult.Page(
                    data = searchUserPage.searchUsers,
                    prevKey = if (searchUserPage.hasPrevious) page - 1 else null,
                    nextKey = if (searchUserPage.hasNext) page + 1 else null,
                )
            }.onFailure { exception ->
                return LoadResult.Error(exception)
            }
        return LoadResult.Invalid()
    }
}
