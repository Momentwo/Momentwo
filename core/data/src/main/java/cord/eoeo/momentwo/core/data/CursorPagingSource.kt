package cord.eoeo.momentwo.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import java.io.IOException

abstract class CursorPagingSource<T : Any> : PagingSource<Int, T>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val nextCursor = params.key ?: 0

        return try {
            loadPage(nextCursor)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    protected abstract suspend fun loadPage(nextCursor: Int): LoadResult<Int, T>
}
