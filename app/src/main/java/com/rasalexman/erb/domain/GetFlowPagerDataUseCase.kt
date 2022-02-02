package com.rasalexman.erb.domain

import androidx.paging.PagingData
import androidx.paging.map
import com.rasalexman.erb.common.IUseCase
import com.rasalexman.erb.data.IPageRepository
import com.rasalexman.erb.data.PageRepository
import com.rasalexman.erb.models.RecyclerItemUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFlowPagerDataUseCase(
    private val pageRepository: IPageRepository = PageRepository()
) : IGetFlowPagerDataUseCase {
    override suspend fun invoke(): Flow<PagingData<RecyclerItemUI>> {
        return pageRepository.getPageFlowItemsSource().map { pageData ->
            pageData.map { it.convert() }
        }
    }

}

interface IGetFlowPagerDataUseCase : IUseCase.SOut<Flow<PagingData<RecyclerItemUI>>>