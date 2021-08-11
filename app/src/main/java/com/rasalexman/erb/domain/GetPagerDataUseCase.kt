package com.rasalexman.erb.domain

import androidx.paging.Pager
import com.rasalexman.erb.common.IUseCase
import com.rasalexman.erb.data.IPageRepository
import com.rasalexman.erb.data.PageRepository
import com.rasalexman.erb.models.PageModel

class GetPagerDataUseCase(
    private val pageRepository: IPageRepository = PageRepository()
) : IGetPagerDataUseCase {
    override fun invoke(): Pager<Int, PageModel> {
        return pageRepository.getPagerItemsSource()
    }

}

interface IGetPagerDataUseCase : IUseCase.Out<Pager<Int, PageModel>>