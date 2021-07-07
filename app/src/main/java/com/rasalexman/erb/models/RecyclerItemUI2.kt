package com.rasalexman.erb.models

import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.R

data class RecyclerItemUI2(
    override val id: String,
    override val title: String
) : IRecyclerItem, IBindingModel {
    override val layoutResId: Int
        get() = R.layout.item_recycler2
}