package com.rasalexman.easyrecyclerbinding.adapters

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

internal class DataBindingRecyclerAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val erbAdapter: ErbAdapter<ItemType, BindingType>
) : RecyclerView.Adapter<BindingViewHolder>(),
    IErbAdapter<ItemType, BindingType> by erbAdapter {

    override fun getItemCount(): Int {
        return erbAdapter.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return erbAdapter.getItemViewType(position)
    }

    override fun getAdapterItems(): List<ItemType> {
        return erbAdapter.getAdapterItems()
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
        erbAdapter.clearAdapter()
    }
}