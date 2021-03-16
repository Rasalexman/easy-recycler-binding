package com.rasalexman.erb.ui.viewpager2example

import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.createRecyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.Vp2ExampleFragmentBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment
import com.rasalexman.erb.ui.viewpager2example.pages.FirstPageViewModel
import com.rasalexman.erb.ui.viewpager2example.pages.SecondPageViewModel

class ViewPager2ExampleFragment : BaseBindingFragment<Vp2ExampleFragmentBinding, ViewPager2ExampleViewModel>() {
    override val layoutId: Int get() = R.layout.vp2_example_fragment
    override val viewModel: ViewPager2ExampleViewModel by viewModels()

    private val firstPageViewModel: FirstPageViewModel by viewModels()
    private val secondPageViewModel: SecondPageViewModel by viewModels()

    override fun initBinding(binding: Vp2ExampleFragmentBinding) {
        super.initBinding(binding)
        viewModel.items.value = listOf(firstPageViewModel, secondPageViewModel)

        binding.vpConfig = createRecyclerMultiConfig {
            itemId = BR.vm
        }
    }
}