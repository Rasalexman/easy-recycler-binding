package com.rasalexman.easyrecyclerbinding

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
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
        clearLastJob()
        pagerData?.let { freshPagingData ->
            (adapter as? PagingDataAdapter<ItemType, BindingViewHolder>)?.let { pagingDataAdapter ->
                lifecycleOwnerWeak.get()?.run {
                    pagingDataAdapter.submitData(lifecycle, freshPagingData)
                } ?: launchSubmit(freshPagingData, pagingDataAdapter)
            }
        }
    }

    private fun launchSubmit(freshPagingData: PagingData<ItemType>, adapter: PagingDataAdapter<ItemType, BindingViewHolder>) {
        lastJob = launch(coroutineContext) {
            adapter.submitData(freshPagingData)
        }
    }

    override fun clearLastJob() {
        lastJob?.cancel()
        lastJob = null
        supervisorJob.cancelChildren()
    }

    override fun clear() {
        clearLastJob()
        lifecycleOwnerWeak.clear()
    }

    override fun areContentsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }

    override fun areItemsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
        return oldItem == newItem
    }
}