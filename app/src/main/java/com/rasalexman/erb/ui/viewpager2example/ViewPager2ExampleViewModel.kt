package com.rasalexman.erb.ui.viewpager2example

import androidx.lifecycle.MutableLiveData
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.ui.base.BasePagesViewModel

class ViewPager2ExampleViewModel : BasePagesViewModel() {

    val items: MutableLiveData<List<IBindingModel>> = MutableLiveData()
}