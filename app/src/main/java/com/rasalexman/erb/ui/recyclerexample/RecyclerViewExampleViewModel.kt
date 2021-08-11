package com.rasalexman.erb.ui.recyclerexample

import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections

class RecyclerViewExampleViewModel : BaseItemsViewModel() {

    fun onShowSelectedItemFragment(item: IBindingModel) {
        (item as? IRecyclerItem)?.let {
            navigationState.value = MainFragmentDirections.showSelectedFragment(selectedItem = it.title)
        }
    }

}