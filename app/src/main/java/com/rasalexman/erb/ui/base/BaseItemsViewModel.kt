package com.rasalexman.erb.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseItemsViewModel : BaseViewModel() {
    val items: MutableLiveData<MutableList<IBindingModel>> = MutableLiveData()

    init {
        createItems()
    }

    fun createItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val itemsList = mutableListOf<IBindingModel>()
            val existedList = items.value ?: mutableListOf()
            val itemsCount = existedList.size
            repeat(100) {
                itemsList.add(
                    if(it%2 == 0) {
                        RecyclerItemUI(
                            title = "This is a title number ${itemsCount+it}",
                            id = UUID.randomUUID().toString()
                        )
                    } else {
                        RecyclerItemUI2(
                            title = "This is a title number ${itemsCount+it}",
                            id = UUID.randomUUID().toString()
                        )
                    }
                )
            }
            existedList.addAll(itemsList)
            items.postValue(existedList)
        }
    }
}