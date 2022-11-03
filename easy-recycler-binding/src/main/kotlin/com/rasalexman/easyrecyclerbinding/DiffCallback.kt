package com.rasalexman.easyrecyclerbinding

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasalexman.easyrecyclerbinding.adapters.ItemsBinderAdapter
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class DiffCallback<ItemType : Any> : DiffUtil.Callback(), ISetData<ItemType>, CoroutineScope {

    private var oldData: List<ItemType>? = null
    private var newData: List<ItemType>? = null

    private var lastDiffUtil: DiffUtil.DiffResult? = null
    private var lastJob: Job? = null

    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + supervisorJob

    @Suppress("UNCHECKED_CAST")
    override fun setData(fresh: List<ItemType>?, adapter: RecyclerView.Adapter<*>) {
        clearLastJob()

        fresh?.let { data ->
            val itemsAdapter = adapter as ItemsBinderAdapter<ItemType>
            lastJob = launch {
                //val startTime = System.currentTimeMillis()
                //println("-----> start processing ")
                val localOldItems = itemsAdapter.getAdapterItems()
                val localNewItems = data.toList()
                oldData = localOldItems
                newData = localNewItems

                lastDiffUtil = processCalculationAsync()
                lastDiffUtil?.let {
                    if(this.isActive) {
                        itemsAdapter.setAdapterItems(localNewItems)
                        it.dispatchUpdatesTo(adapter)
                    }
                }
                //val finishTime = System.currentTimeMillis() - startTime
                //println("-----> finish processing in ${finishTime}ms")
            }
        }
    }

    override fun clearLastJob() {
        lastDiffUtil = null
        lastJob?.cancel()
        lastJob = null
        coroutineContext.cancelChildren()
    }

    protected open suspend fun processCalculationAsync() = runInterruptible(Dispatchers.Default) {
        DiffUtil.calculateDiff(this@DiffCallback)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData?.getOrNull(oldItemPosition)
        val newItem = newData?.getOrNull(newItemPosition)
        return if (oldItem != null && newItem != null) {
            areContentsTheSame(oldItem, newItem)
        } else if (oldItem == null && newItem == null) {
            true
        } else {
            false
        }
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData?.getOrNull(oldItemPosition)
        val newItem = newData?.getOrNull(newItemPosition)
        return if (oldItem != null && newItem != null) {
            areItemsTheSame(oldItem, newItem)
        } else {
            oldItem == null && newItem == null
        }
    }

    open fun areContentsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
        return oldItem == newItem
    }

    open fun areItemsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
        return oldItem == newItem
    }

    override fun getOldListSize(): Int {
        return oldData?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newData?.size ?: 0
    }

    override fun clear() {
        clearLastJob()
        oldData = null
        newData = null
    }
}