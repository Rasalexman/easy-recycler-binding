package com.rasalexman.erb.ui.selected

import androidx.fragment.app.viewModels
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.FragmentSelectedBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment

class SelectedFragment : BaseBindingFragment<FragmentSelectedBinding, SelectedViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_selected
    override val viewModel: SelectedViewModel by viewModels()

}