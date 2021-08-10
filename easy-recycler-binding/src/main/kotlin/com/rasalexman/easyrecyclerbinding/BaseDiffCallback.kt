package com.rasalexman.easyrecyclerbinding

import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class BaseDiffCallback<ItemType : Any> : ISetData<ItemType>, CoroutineScope {

    protected var lastJob: Job? = null
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + supervisorJob

    override fun clear() {
        clearLastJob()
    }

    private fun clearLastJob() {
        lastJob?.cancel()
        lastJob = null
        supervisorJob.cancelChildren()
    }

    override fun setData(fresh: List<ItemType>?, adapter: RecyclerView.Adapter<*>) = Unit
    override fun setPageData(pagerData: PagingData<ItemType>?, adapter: RecyclerView.Adapter<*>) = Unit
}

interface IDiffCallback

interface ISetData<ItemType : Any> : IDiffCallback {
    fun setPageData(pagerData: PagingData<ItemType>?, adapter: RecyclerView.Adapter<*>)
    fun setData(fresh: List<ItemType>?, adapter: RecyclerView.Adapter<*>)
    fun clear()
}