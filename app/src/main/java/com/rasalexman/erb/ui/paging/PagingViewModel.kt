package com.rasalexman.erb.ui.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.createStateAdapter
import com.rasalexman.easyrecyclerbinding.pagingConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.ItemRecyclerBinding
import com.rasalexman.erb.domain.GetFlowPagerDataUseCase
import com.rasalexman.erb.domain.IGetFlowPagerDataUseCase
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.LoadingHeaderItem
import com.rasalexman.erb.models.LoadingItem
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.ui.base.BasePagesViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PagingViewModel : BasePagesViewModel(), IBindingModel {

    override val layoutResId: Int
        get() = R.layout.rv_view_paging_fragment

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

    val pagingItems: LiveData<PagingData<RecyclerItemUI>> by lazy {
        pagingItems2.asLiveData(Dispatchers.Default)
    }

    fun createPagingConfig() = pagingConfig<RecyclerItemUI, ItemRecyclerBinding>  {
        itemId = BR.item
        layoutId = R.layout.item_recycler

        onItemClick = { item, _ ->
            onShowSelectedItemFragment(item)
        }

        stateFooterAdapter = createStateAdapter(LoadingItem()) {
            itemId = BR.item
        }
        stateHeaderAdapter = createStateAdapter(LoadingHeaderItem()) {
            itemId = BR.item
        }
    }
}