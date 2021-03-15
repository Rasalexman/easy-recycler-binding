package com.rasalexman.erb.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.rasalexman.easyrecyclerbinding.createBindingWithViewModel
import com.rasalexman.erb.BR

abstract class BaseBindingFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<VM>() {

    var binding: B? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createBindingWithViewModel<B, VM>(
            layoutId = layoutId,
            container = container,
            viewModel = viewModel,
            viewModelBRId = BR.vm
        ).also {
            binding = it
            initBinding(it)
        }.root
    }

    open fun initBinding(binding: B) = Unit

    override fun onDestroyView() {
        binding?.unbind()
        binding = null
        super.onDestroyView()
    }
}