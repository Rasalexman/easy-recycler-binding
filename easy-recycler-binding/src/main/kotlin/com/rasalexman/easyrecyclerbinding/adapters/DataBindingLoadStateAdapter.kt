package com.rasalexman.easyrecyclerbinding.adapters

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class DataBindingLoadStateAdapter<ItemType : Any>(
    private val erbAdapter: ErbAdapter<ItemType, *>
) : LoadStateAdapter<BindingViewHolder>() {

    init {
        erbAdapter.onGetItemsCountHandler = this::getItemCount
    }

    override fun onBindViewHolder(holder: BindingViewHolder, loadState: LoadState) {
        erbAdapter.onBindViewHolder(holder, 0)
    }

    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        return loadState is LoadState.Loading
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BindingViewHolder {
        return erbAdapter.onCreateViewHolder(parent, erbAdapter.getItemViewType(0))
    }
}