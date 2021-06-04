package com.rasalexman.erb.ui.viewpager2example.pages

import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.recyclerConfig
import com.rasalexman.easyrecyclerbinding.recyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.ItemRecyclerBinding
import com.rasalexman.erb.models.SimpleRecyclerItemUI
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import java.util.*
import kotlin.random.Random

class SecondPageViewModel : BaseItemsViewModel(), IBindingModel {
    override val layoutResId: Int
        get() = R.layout.item_vp2_second_page

    fun createRvConfig() = recyclerConfig<SimpleRecyclerItemUI, ItemRecyclerBinding> {
        itemId = BR.item
        layoutId = R.layout.item_recycler

        onItemClick = { item, _ ->
            navigationState.value = MainFragmentDirections.showSelectedFragment(selectedItem = item.title)
        }
    }

    override fun itemsCreator(position: Int): IBindingModel {
        return SimpleRecyclerItemUI(
            title = UUID.randomUUID().toString().take(14),
            id = Random.nextInt(100, 100000).toString()
        )
    }

}