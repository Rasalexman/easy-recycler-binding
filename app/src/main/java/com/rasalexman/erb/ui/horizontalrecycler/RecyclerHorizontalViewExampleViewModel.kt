package com.rasalexman.erb.ui.horizontalrecycler

import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import java.util.*
import kotlin.random.Random

class RecyclerHorizontalViewExampleViewModel : BaseItemsViewModel() {

    override suspend fun itemsCreator(position: Int): IRecyclerItem {
        return TopItemUI(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString().take(6)
        )
    }

    fun onShowSelectedItemFragment(item: TopItemUI) {
        navigationState.value = MainFragmentDirections.showSelectedFragment(selectedItem = item.title)
    }
}