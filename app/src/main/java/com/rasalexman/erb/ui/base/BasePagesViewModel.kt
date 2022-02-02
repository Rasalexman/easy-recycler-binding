package com.rasalexman.erb.ui.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rasalexman.easyrecyclerbinding.PageSelectionListener
import com.rasalexman.easyrecyclerbinding.ScrollPosition

abstract class BasePagesViewModel : BaseViewModel(), PageSelectionListener {
    protected open val threshold: Int = 7
    open val selectedPage: MutableLiveData<Int> = MutableLiveData(0)
    val scrollPosition: LiveData<ScrollPosition> = MutableLiveData(ScrollPosition())
    open val visibleThresholds: MutableLiveData<Int> by lazy {
        MutableLiveData(threshold)
    }

    override fun onPageSelected(position: Int) {
        Log.d(TAG_PAGE_SELECT, "Selected ViewPager tab = ${selectedPage.value}")
    }

    companion object {
        private const val TAG_PAGE_SELECT = "TAG_PAGE_SELECT"
    }
}