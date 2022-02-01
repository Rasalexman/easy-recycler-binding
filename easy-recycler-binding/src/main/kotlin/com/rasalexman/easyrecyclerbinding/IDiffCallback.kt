package com.rasalexman.easyrecyclerbinding

import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rasalexman.easyrecyclerbinding.adapters.BindingViewHolder

interface IDiffCallback

interface ISetData<ItemType : Any> : IDiffCallback {
    fun setPageData(pagerData: PagingData<ItemType>?, adapter: PagingDataAdapter<ItemType, BindingViewHolder>) = Unit
    fun setData(fresh: List<ItemType>?, adapter: RecyclerView.Adapter<*>) = Unit
    fun clear()
    fun clearLastJob()
}