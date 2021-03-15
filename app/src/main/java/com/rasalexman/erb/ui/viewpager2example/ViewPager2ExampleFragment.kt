package com.rasalexman.erb.ui.viewpager2example

import androidx.fragment.app.viewModels
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.Vp2ExampleFragmentBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment

class ViewPager2ExampleFragment : BaseBindingFragment<Vp2ExampleFragmentBinding, ViewPager2ExampleViewModel>() {
    override val layoutId: Int get() = R.layout.vp2_example_fragment
    override val viewModel: ViewPager2ExampleViewModel by viewModels()
}