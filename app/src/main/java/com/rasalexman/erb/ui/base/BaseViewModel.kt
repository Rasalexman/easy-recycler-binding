package com.rasalexman.erb.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel : ViewModel() {

    val navigationState: MutableStateFlow<NavDirections?> = MutableStateFlow(null)

    fun clearNavigation() {
        navigationState.value = null
    }
}