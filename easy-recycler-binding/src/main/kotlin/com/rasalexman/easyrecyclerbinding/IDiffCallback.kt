package com.rasalexman.easyrecyclerbinding

import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

interface IDiffCallback

interface ISetData<ItemType : Any> : IDiffCallback {
    fun setPageData(pagerData: PagingData<ItemType>?, adapter: RecyclerView.Adapter<*>)
    fun setData(fresh: List<ItemType>?, adapter: RecyclerView.Adapter<*>)
    fun clear()
    fun clearLastJob()
}