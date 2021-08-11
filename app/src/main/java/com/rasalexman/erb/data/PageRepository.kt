package com.rasalexman.erb.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.rasalexman.erb.data.source.IPageService
import com.rasalexman.erb.data.source.PageRemoteSource
import com.rasalexman.erb.data.source.PageService
import com.rasalexman.erb.models.PageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class PageRepository(
    private val pageService: IPageService = PageService()
) : IPageRepository {

    override fun getPageFlowItemsSource(): Flow<PagingData<PageModel>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { PageRemoteSource(pageService) }
        ).flow
    }

    override fun getPagerItemsSource(): Pager<Int, PageModel> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { PageRemoteSource(pageService) }
        )
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 16
    }
}

interface IPageRepository {
    fun getPageFlowItemsSource(): Flow<PagingData<PageModel>>
    fun getPagerItemsSource(): Pager<Int, PageModel>
}