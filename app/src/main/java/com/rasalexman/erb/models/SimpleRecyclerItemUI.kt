package com.rasalexman.erb.models

import android.text.SpannedString
import androidx.databinding.ObservableBoolean
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.R

data class SimpleRecyclerItemUI(
    override val id: String,
    override val title: String
) : IRecyclerItem, IBindingModel {

    var descriptionText: SpannedString = SpannedString(title)
    override val isChecked: ObservableBoolean = ObservableBoolean(false)
    override val layoutResId: Int
        get() = R.layout.item_simple_recycler
}
