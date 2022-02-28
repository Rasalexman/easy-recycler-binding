package com.rasalexman.erb.ui.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.Navigation

abstract class BaseHostFragment<VB : ViewDataBinding, VM : BaseViewModel> : BaseBindingFragment<VB, VM>(), IBaseHost {

    protected val navController: NavController
        get() = Navigation.findNavController(
            requireActivity(),
            navControllerId
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbindNavController()
        bindNavController()
        //log { "Navigator is binded with tag ${getNavigator(navigatorTag).tag}" }
    }

    override fun unbindNavController() = Unit
    override fun bindNavController() = Unit

    override fun onDestroyView() {
        unbindNavController()
        super.onDestroyView()
    }
}
