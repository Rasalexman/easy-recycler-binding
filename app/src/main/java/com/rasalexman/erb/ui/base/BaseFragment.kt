package com.rasalexman.erb.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.rasalexman.erb.R

abstract class BaseFragment<VM : BaseViewModel> : Fragment() {

    abstract val layoutId: Int
    abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNavigation(viewModel)
    }

    protected fun observeNavigation(viewModel: BaseViewModel) {
        viewModel.navigationState.observe(viewLifecycleOwner) {
            navigateToDirection(viewModel, it)
        }
    }

    protected open fun navigateToDirection(viewModel:BaseViewModel, direction: NavDirections?) {
        direction?.let {
            viewModel.clearNavigation()
            try {
                this.findNavController().navigate(it)
            } catch (e: Exception) {
                requireActivity().findNavController(R.id.nav_host_fragment).navigate(it)
            }
        }
    }
}