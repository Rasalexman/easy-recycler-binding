package com.rasalexman.erb.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect

abstract class BaseFragment<VM : BaseViewModel> : Fragment() {

    abstract val layoutId: Int
    abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigationState.observe(viewLifecycleOwner, Observer {
            navigateToDirection(it)
        })
    }

    protected open fun navigateToDirection(direction: NavDirections?) {
        direction?.let {
            viewModel.clearNavigation()
            this.findNavController().navigate(it)
        }
    }
}