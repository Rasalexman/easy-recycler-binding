package com.rasalexman.erb.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rasalexman.erb.models.RecyclerItemUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseItemsViewModel : BaseViewModel() {
    val items: MutableLiveData<MutableList<RecyclerItemUI>> = MutableLiveData()

    init {
        createItems()
    }

    fun createItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val itemsList = mutableListOf<RecyclerItemUI>()
            val existedList = items.value ?: mutableListOf()
            val itemsCount = existedList.size
            repeat(100) {
                itemsList.add(
                    RecyclerItemUI(
                        title = "This is a title number ${itemsCount+it}",
                        id = UUID.randomUUID().toString()
                    )
                )
            }
            existedList.addAll(itemsList)
            items.postValue(existedList)
        }
    }
}