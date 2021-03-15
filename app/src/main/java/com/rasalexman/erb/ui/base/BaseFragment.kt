package com.rasalexman.erb.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect

abstract class BaseFragment<VM : BaseViewModel> : Fragment() {

    abstract val layoutId: Int
    abstract val viewModel: VM

    init {
        lifecycleScope.launchWhenCreated {
            viewModel.navigationState.collect(::navigateToDirection)
        }
    }

    protected open fun navigateToDirection(direction: NavDirections?) {
        direction?.let {
            viewModel.clearNavigation()
            this.findNavController().navigate(it)
        }
    }
}