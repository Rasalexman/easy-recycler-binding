package com.rasalexman.erb.ui.viewpager2example.pages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.erb.R
import com.rasalexman.erb.ui.base.BaseViewModel

class FirstPageViewModel : BaseViewModel(), IBindingModel {

    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")

    val authText: MutableLiveData<String> = MutableLiveData("Enter login data")

    val isButtonEnabled = login.switchMap { loginText ->
        password.map { passwordText ->
            val isEnabled = loginText.length > 5 && passwordText.length > 5
            isEnabled
        }
    }

    fun onSingInButtonClicked() {
        val authDescription = "login = ${login.value} \n password = ${password.value}"
        authText.value = (authDescription)
    }

    override val layoutResId: Int
        get() = R.layout.item_vp2_first_page
}