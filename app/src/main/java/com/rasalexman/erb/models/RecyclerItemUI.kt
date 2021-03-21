package com.rasalexman.erb.models

import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.R

data class RecyclerItemUI(
    val id: String,
    val title: String
) : IBindingModel {
    override val layoutResId: Int
        get() = R.layout.item_recycler
}