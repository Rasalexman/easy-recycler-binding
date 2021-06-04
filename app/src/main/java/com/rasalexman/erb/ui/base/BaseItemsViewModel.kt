package com.rasalexman.erb.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

abstract class BaseItemsViewModel : BasePagesViewModel() {

    open val items: MutableLiveData<MutableList<IBindingModel>> = MutableLiveData()

    init {
        createItems()
    }

    fun createItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val existedList = items.value ?: mutableListOf()
            if(existedList.isEmpty()) {
                val itemsList = mutableListOf<IBindingModel>()
                val itemCounts = Random.nextInt(20, 100)
                repeat(itemCounts) {
                    itemsList.add(itemsCreator(it))
                }
                existedList.addAll(itemsList)
                items.postValue(existedList)
            }
        }
    }

    protected open fun itemsCreator(position: Int): IBindingModel {
        val nextId = Random.nextInt(100, 100000).toString()
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