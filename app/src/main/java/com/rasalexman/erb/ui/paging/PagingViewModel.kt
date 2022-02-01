package com.rasalexman.erb.ui.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.domain.GetFlowPagerDataUseCase
import com.rasalexman.erb.domain.IGetFlowPagerDataUseCase
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.ui.base.BasePagesViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PagingViewModel : BasePagesViewModel() {

    private val getFlowPagerDataUseCase: IGetFlowPagerDataUseCase = GetFlowPagerDataUseCase()
    //private val getPagerDataUseCase: IGetPagerDataUseCase = GetPagerDataUseCase()

    private val pagingItems2 = flow {
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