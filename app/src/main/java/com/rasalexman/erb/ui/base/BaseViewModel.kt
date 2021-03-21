package com.rasalexman.erb.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections

abstract class BaseViewModel : ViewModel() {

    val navigationState: MutableLiveData<NavDirections?> = MutableLiveData(null)

    fun clearNavigation() {
        navigationState.value = null
    }
}