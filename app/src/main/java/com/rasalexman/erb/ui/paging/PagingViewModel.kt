package com.rasalexman.erb.ui.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertFooterItem
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.domain.GetFlowPagerDataUseCase
import com.rasalexman.erb.domain.GetPagerDataUseCase
import com.rasalexman.erb.domain.IGetFlowPagerDataUseCase
import com.rasalexman.erb.domain.IGetPagerDataUseCase
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.LoadingItem
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class PagingViewModel : BaseItemsViewModel() {

    private val getFlowPagerDataUseCase: IGetFlowPagerDataUseCase = GetFlowPagerDataUseCase()
    private val getPagerDataUseCase: IGetPagerDataUseCase = GetPagerDataUseCase()

    val pagingItems2 = flow {
        val pageFlow = getFlowPagerDataUseCase().cachedIn(viewModelScope)
        emitAll(pageFlow)
    }.flowOn(Dispatchers.Default)

    fun onShowSelectedItemFragment(item: IBindingModel) {
        (item as? IRecyclerItem)?.let {
            navigationState.value = MainFragmentDirections.showSelectedFragment(selectedItem = it.title)
        }
    }

    val pagingItems: LiveData<PagingData<IRecyclerItem>> by lazy {
        pagingItems2.asLiveData(Dispatchers.Default)
    }
}