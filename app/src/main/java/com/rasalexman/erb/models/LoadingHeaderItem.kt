package com.rasalexman.erb.models

import androidx.paging.LoadState
import com.rasalexman.easyrecyclerbinding.ILoadStateModel
import com.rasalexman.erb.R

data class LoadingHeaderItem(
    override var loadState: LoadState = LoadState.Loading
) : ILoadStateModel {
    override val layoutResId: Int
        get() = R.layout.item_page_header_loading
}
