package com.rasalexman.erb.ui.main.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.rasalexman.erb.BR

abstract class BaseBindingFragment<B : ViewDataBinding, VM : ViewModel> : BaseFragment<VM>() {

    var binding: B? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<B>(inflater, layoutId,  container, false).also{
            binding = it
            it.lifecycleOwner = viewLifecycleOwner
            it.setVariable(BR.vm, viewModel)
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