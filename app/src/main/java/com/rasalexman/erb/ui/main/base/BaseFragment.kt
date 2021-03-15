package com.rasalexman.erb.ui.main.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

abstract class BaseFragment<VM : ViewModel> : Fragment() {

    abstract val layoutId: Int
    abstract val viewModel: VM?
}