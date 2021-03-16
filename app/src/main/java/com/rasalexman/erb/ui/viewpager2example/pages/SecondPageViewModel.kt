package com.rasalexman.erb.ui.viewpager2example.pages

import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.recyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.ui.base.BaseItemsViewModel

class SecondPageViewModel : BaseItemsViewModel(), IBindingModel {
    override val layoutResId: Int
        get() = R.layout.item_vp2_second_page

    fun createRvConfig() = recyclerMultiConfig {
        itemId = BR.item
    }

}