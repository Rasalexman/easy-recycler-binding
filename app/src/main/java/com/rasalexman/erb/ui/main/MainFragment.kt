package com.rasalexman.erb.ui.main

import androidx.fragment.app.viewModels
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.MainFragmentBinding
import com.rasalexman.erb.ui.main.base.BaseBindingFragment

class MainFragment : BaseBindingFragment<MainFragmentBinding, MainViewModel>() {

    override val viewModel: MainViewModel by viewModels<MainViewModel>()
    override val layoutId: Int get() = R.layout.main_fragment
}