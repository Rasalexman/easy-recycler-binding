package com.rasalexman.easyrecyclerbinding

import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.rasalexman.easyrecyclerbinding.adapters.BindingViewHolder
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

open class DiffItemsCallback<ItemType : Any>(
    lifecycleOwner: LifecycleOwner?
) : DiffUtil.ItemCallback<ItemType>(), ISetData<ItemType>, CoroutineScope {

    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + supervisorJob

    private val lifecycleOwnerWeak: WeakReference<LifecycleOwner> = WeakReference(lifecycleOwner)

    @Suppress("UNCHECKED_CAST")
    override fun setPageData(pagerData: PagingData<ItemType>?, adapter: PagingDataAdapter<ItemType, BindingViewHolder>) {
        clearLastJob()
        pagerData?.let { freshPagingData ->
            lifecycleOwnerWeak.get()?.apply {
                adapter.submitData(lifecycle, freshPagingData)
            } ?: launchSubmit(freshPagingData, adapter)
        }
    }

    private fun launchSubmit(
        freshPagingData: PagingData<ItemType>,
        adapter: PagingDataAdapter<ItemType, BindingViewHolder>
    ) {
        launch {
            adapter.submitData(freshPagingData)
        }
    }

    override fun clearLastJob() {
        coroutineContext.cancelChildren()
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