package com.rasalexman.erb.models

import androidx.databinding.ObservableBoolean
import androidx.paging.LoadState
import com.rasalexman.easyrecyclerbinding.ILoadStateModel
import com.rasalexman.erb.R

data class LoadingItem(
    override val id: String = "",
    override val title: String = "",
    override var loadState: LoadState = LoadState.Loading
) : ILoadStateModel, IRecyclerItem {
    override val isChecked: ObservableBoolean = ObservableBoolean(false)
    override val layoutResId: Int
        get() = R.layout.item_page_loading

    val isLoading: Boolean
        get() = true

    val isError: Boolean
        get() = false
}
