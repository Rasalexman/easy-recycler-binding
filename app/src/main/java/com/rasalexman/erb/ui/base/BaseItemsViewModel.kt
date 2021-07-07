package com.rasalexman.erb.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

abstract class BaseItemsViewModel : BasePagesViewModel() {

    open val items: MutableLiveData<List<IRecyclerItem>> = MutableLiveData()

    init {
        createItems()
    }

    fun createItems(minItems: Int = 20, maxItems: Int = 100) {
        viewModelScope.launch {
            val itemsList = mutableListOf<IRecyclerItem>()
            withContext(Dispatchers.IO) {
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
                println("------> itemsList size = ${itemsList.size}")
                items.postValue(itemsList)
            }
        }
    }

    fun clearItems() {
        items.postValue(emptyList())
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
}