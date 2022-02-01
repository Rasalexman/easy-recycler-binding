package com.rasalexman.erb.models

import androidx.paging.LoadState
import com.rasalexman.easyrecyclerbinding.ILoadStateModel
import com.rasalexman.erb.R

data class LoadingItem(
    override var loadState: LoadState = LoadState.NotLoading(endOfPaginationReached = false)
) : ILoadStateModel {

    override val layoutResId: Int
        get() = R.layout.item_page_loading

    val isLoading: Boolean
        get() = true

    val isError: Boolean
        get() = false
}
