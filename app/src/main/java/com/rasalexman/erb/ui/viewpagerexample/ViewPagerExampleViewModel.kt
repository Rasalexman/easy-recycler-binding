package com.rasalexman.erb.ui.viewpagerexample

import androidx.lifecycle.MutableLiveData
import com.rasalexman.erb.ui.base.BaseItemsViewModel

class ViewPagerExampleViewModel : BaseItemsViewModel() {

    val selectedTab: MutableLiveData<Int> = MutableLiveData()
}