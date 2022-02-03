package com.rasalexman.erb.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import com.rasalexman.erb.ui.main.MainFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

abstract class BaseItemsViewModel : BasePagesViewModel() {

    open val items: MutableLiveData<List<IRecyclerItem>> = MutableLiveData()

    open val itemsCount: LiveData<String> by lazy {
        items.map {
            "Items count: ${it.size}"
        }
    }

    init {
        createItems()
    }

    fun createItems(minItems: Int = 1000, maxItems: Int = 30000) {
        viewModelScope.launch {
            val itemsList = mutableListOf<IRecyclerItem>()
            withContext(Dispatchers.IO) {
                showLoading()
                val itemCounts = Random.nextInt(minItems, maxItems)
                repeat(itemCounts) {
                    itemsList.add(itemsCreator(it))
                }
                /*val lastList = items.value.orEmpty()
                if(lastList.isNotEmpty()) {
                    val subs = lastList.subList(0, lastList.size/2)
                    //val randomIndex = Random.nextInt(0, minItems)
                    itemsList.addAll(0, subs)
                }*/
                println("------> createItemsList size = ${itemsList.size}")
                postItems(itemsList)
            }
        }
    }

    fun addItems(minItems: Int = 14, maxItems: Int = 100, atFirst: Boolean = true) {
        showLoading()
        viewModelScope.launch {
            val itemsList = mutableListOf<IRecyclerItem>()
            withContext(Dispatchers.IO) {
                val itemCounts = Random.nextInt(minItems, maxItems)
                repeat(itemCounts) {
                    itemsList.add(itemsCreator(it))
                }
                val lastList = items.value.orEmpty().toMutableList()
                if(atFirst) lastList.addAll(0, itemsList)
                else lastList.addAll(itemsList)
                println("------> [added] at first = $atFirst | size = ${lastList.size}")
                postItems(lastList)
            }
        }
    }

    fun onRemoveClicked(atFirst: Boolean = true) {
        showLoading()
        viewModelScope.launch {
            val lastList = items.value.orEmpty().toMutableList()
            if(lastList.isNotEmpty()) {
                val maxItems = lastList.size
                val itemCounts = if(maxItems == 1) maxItems else Random.nextInt(1, maxItems)
                val subList = if(atFirst) lastList.take(itemCounts) else lastList.takeLast(itemCounts)
                lastList.removeAll(subList)
                println("----> [remove] at first = $atFirst | count = $itemCounts | allCount = ${lastList.size}")
            }
            postItems(lastList)
        }
    }

    fun clearItems() {
        postItems(emptyList())
    }

    protected open fun postItems(freshItems: List<IRecyclerItem>) {
        items.postValue(freshItems)
        hideLoading()
    }

    protected open suspend fun itemsCreator(position: Int): IRecyclerItem {
        val nextId = UUID.randomUUID().toString()
        return if (position % 2 == 0) {
            RecyclerItemUI(
                title = UUID.randomUUID().toString().take(14),
                id = nextId
            )
        } else {
            RecyclerItemUI2(
                title = UUID.randomUUID().toString().take(20),
                id = nextId
            )
        }
    }

    fun onShowSelectedBindingItemFragment(item: IBindingModel) {
        (item as? IRecyclerItem)?.let {
            onShowSelectedRecyclerItemFragment(it)
        }
    }

    fun onShowSelectedRecyclerItemFragment(item: IRecyclerItem) {
        navigationState.value = MainFragmentDirections.showSelectedFragment(selectedItem = item.title)
    }
}