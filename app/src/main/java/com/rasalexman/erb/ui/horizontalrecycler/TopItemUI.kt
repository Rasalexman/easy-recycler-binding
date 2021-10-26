package com.rasalexman.erb.ui.horizontalrecycler

import androidx.databinding.ObservableBoolean
import com.rasalexman.erb.models.IRecyclerItem

data class TopItemUI(
    override val id: String,
    override val title: String
) : IRecyclerItem {
    override val layoutResId: Int = -1
    override val isChecked: ObservableBoolean = ObservableBoolean(false)
}