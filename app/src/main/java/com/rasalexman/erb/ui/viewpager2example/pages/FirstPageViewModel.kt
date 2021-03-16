package com.rasalexman.erb.ui.viewpager2example.pages

import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.R
import com.rasalexman.erb.ui.base.BaseViewModel

class FirstPageViewModel : BaseViewModel(), IBindingModel {


    fun onSingInButtonClicked() {

    }

    fun onSingUpButtonClicked() {

    }

    override val layoutResId: Int
        get() = R.layout.item_vp2_first_page
}