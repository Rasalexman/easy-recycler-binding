package com.rasalexman.erb.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseItemsViewModel : BasePagesViewModel() {
    open val items: MutableLiveData<MutableList<IBindingModel>> = MutableLiveData()

    init {
        createItems()
    }

    fun createItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val itemsList = mutableListOf<IBindingModel>()
            val existedList = items.value ?: mutableListOf()
            val itemsCount = existedList.size
            repeat(100) {
                itemsList.add(itemsCreator(itemsCount, it))
            }
            existedList.addAll(itemsList)
            items.postValue(existedList)
        }
    }

    private fun itemsCreator(itemsCount: Int, position: Int): IBindingModel {
        val nextPositionNumber = itemsCount + position
        val nextId = UUID.randomUUID().toString()
        return if (position % 2 == 0) {
            RecyclerItemUI(
                    title = "This is a title number $nextPositionNumber",
                    id = nextId
            )
        } else {
            RecyclerItemUI2(
                    title = "This is a title number $nextPositionNumber",
                    id = nextId
            )
        }
    }
}