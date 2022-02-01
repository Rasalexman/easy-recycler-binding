package com.rasalexman.easyrecyclerbinding.adapters

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import com.rasalexman.easyrecyclerbinding.DiffItemsCallback
import java.lang.ref.WeakReference

internal class DataBindingPagingDataAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val erbAdapter: ErbAdapter<ItemType, BindingType>,
    diffUtilCallback: DiffItemsCallback<ItemType>
) : PagingDataAdapter<ItemType, BindingViewHolder>(diffUtilCallback),
    IErbAdapter<ItemType, BindingType> by erbAdapter {

    private val diffItemsUtilsWeakRef = WeakReference(diffUtilCallback)

    fun getItemsDiffCallback(): DiffItemsCallback<ItemType>? {
        return diffItemsUtilsWeakRef.get()
    }

    init {
        erbAdapter.onGetItemHandler = ::getItem
        erbAdapter.onGetItemsCountHandler = ::getItemCount
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return erbAdapter.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return erbAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        erbAdapter.onBindViewHolder(holder, position)
    }

    override fun onViewRecycled(holder: BindingViewHolder) {
        erbAdapter.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun clearAdapter() {
        diffItemsUtilsWeakRef.clear()
        erbAdapter.clearAdapter()
    }
}