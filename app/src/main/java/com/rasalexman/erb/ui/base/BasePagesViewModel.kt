package com.rasalexman.erb.ui.base

import android.view.View
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
    val isLoading: MutableLiveData<Int> = MutableLiveData(View.GONE)

    protected fun showLoading() {
        isLoading.postValue(View.VISIBLE)
    }

    protected fun hideLoading() {
        isLoading.postValue(View.GONE)
    }

    override fun onPageSelected(position: Int) {
        println("-------> Selected ViewPager tab = ${selectedPage.value}")
    }
}