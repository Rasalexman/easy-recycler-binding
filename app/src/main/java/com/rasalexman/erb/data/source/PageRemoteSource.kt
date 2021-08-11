package com.rasalexman.erb.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rasalexman.erb.models.PageModel
import kotlinx.coroutines.delay
import java.io.IOException

class PageRemoteSource(
    private val pageService: IPageService
) : PagingSource<Int, PageModel>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PageModel> {
        val position = params.key ?: START_PAGE_INDEX
        return try {

            val repos = pageService.loadItems(position, params.loadSize)
            val nextKey = if (repos.isEmpty()) {
                null
            } else {
                val nextPositionCount = (repos.size / NETWORK_PAGE_SIZE)
                val validPositionCount = nextPositionCount.takeIf { it > 0 } ?: DEFAULT_NEXT_POSITION_COUNT
                position + validPositionCount
            }
            LoadResult.Page(
                data = repos,
                prevKey = if (position == START_PAGE_INDEX) null else position - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PageModel>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val DEFAULT_NEXT_POSITION_COUNT = 1
        private const val START_PAGE_INDEX = 0
        private const val NETWORK_PAGE_SIZE = 10
    }
}