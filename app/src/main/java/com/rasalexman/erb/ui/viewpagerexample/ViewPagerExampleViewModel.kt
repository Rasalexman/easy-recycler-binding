package com.rasalexman.erb.ui.viewpagerexample

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.rasalexman.easyrecyclerbinding.ScrollPosition
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import kotlinx.coroutines.Dispatchers

class ViewPagerExampleViewModel : BaseItemsViewModel() {

    val firstPageScrollPosition: MutableLiveData<ScrollPosition> = MutableLiveData(ScrollPosition())
    val secondPageScrollPosition: MutableLiveData<ScrollPosition> = MutableLiveData(ScrollPosition())

    val firstPageItems by lazy {
        items.switchMap { allItems ->
            liveData(context = Dispatchers.Default) {
                val filteredItems = allItems.filterIsInstance<RecyclerItemUI>()
                emit(filteredItems)
            }
        }
    }

    val secondPageItems by lazy {
        items.switchMap { allItems ->
            liveData(context = Dispatchers.Default) {
                val filteredItems = allItems.filterIsInstance<RecyclerItemUI2>()
                emit(filteredItems)
            }
        }
    }
}