package com.rasalexman.erb.ui.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rasalexman.easyrecyclerbinding.PageSelectionListener

abstract class BaseItemsPageViewModel : BaseItemsViewModel(), PageSelectionListener {
    val selectedTab: MutableLiveData<Int> = MutableLiveData()

    override fun onPageSelected(position: Int) {
        Log.d(TAG_PAGE_SELECT, "Selected ViewPager tab = ${selectedTab.value}")
    }

    companion object {
        private const val TAG_PAGE_SELECT = "TAG_PAGE_SELECT"
    }
}