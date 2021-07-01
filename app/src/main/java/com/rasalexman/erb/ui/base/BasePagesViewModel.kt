package com.rasalexman.erb.ui.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rasalexman.easyrecyclerbinding.PageSelectionListener

abstract class BasePagesViewModel : BaseViewModel(), PageSelectionListener {
    open val selectedPage: MutableLiveData<Int> = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        Log.d(TAG_PAGE_SELECT, "Selected ViewPager tab = ${selectedPage.value}")
    }

    companion object {
        private const val TAG_PAGE_SELECT = "TAG_PAGE_SELECT"
    }
}