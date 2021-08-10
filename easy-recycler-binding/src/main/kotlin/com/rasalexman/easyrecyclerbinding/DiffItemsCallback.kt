package com.rasalexman.easyrecyclerbinding

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

open class DiffItemsCallback<ItemType : Any>(
    lifecycleOwner: LifecycleOwner?
) : DiffUtil.ItemCallback<ItemType>(), ISetData<ItemType>, CoroutineScope {

    private var lastJob: Job? = null
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + supervisorJob

    private val lifecycleOwnerWeak:WeakReference<LifecycleOwner> = WeakReference(lifecycleOwner)

    override fun setData(fresh: List<ItemType>?, adapter: RecyclerView.Adapter<*>) = Unit
    @Suppress("UNCHECKED_CAST")
    override fun setPageData(pagerData: PagingData<ItemType>?, adapter: RecyclerView.Adapter<*>) {
        clear()
        pagerData?.let { freshPagingData ->
            lastJob = lifecycleOwnerWeak.get()?.run {
                lifecycleScope.launch {
                    (adapter as? PagingDataAdapter<ItemType, BindingViewHolder>)?.submitData(lifecycle, freshPagingData)
                }
            } ?: launch {
                (adapter as? PagingDataAdapter<ItemType, BindingViewHolder>)?.submitData(freshPagingData)
            }
        }

    }

    override fun clear() {
        lastJob?.cancel()
        lastJob = null
        supervisorJob.cancelChildren()
    }

    override fun areContentsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }

    override fun areItemsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
        return oldItem == newItem
    }
}