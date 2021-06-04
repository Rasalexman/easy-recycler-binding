package com.rasalexman.erb.ui.viewpager2example

import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.createRecyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.Vp2ExampleFragmentBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment
import com.rasalexman.erb.ui.base.BasePagerBindingFragment
import com.rasalexman.erb.ui.base.BaseViewModel
import com.rasalexman.erb.ui.viewpager2example.pages.FirstPageViewModel
import com.rasalexman.erb.ui.viewpager2example.pages.SecondPageViewModel

class ViewPager2ExampleFragment : BasePagerBindingFragment<Vp2ExampleFragmentBinding, ViewPager2ExampleViewModel>() {
    override val layoutId: Int get() = R.layout.vp2_example_fragment
    override val viewModel: ViewPager2ExampleViewModel by viewModels()

    override val pageTitles: List<String> = listOf("Login", "With Recycler")
    override val pagesVMList: List<BaseViewModel> by lazy {
        listOf(firstPageViewModel, secondPageViewModel)
    }

    private val firstPageViewModel: FirstPageViewModel by viewModels()
    private val secondPageViewModel: SecondPageViewModel by viewModels()

    override fun initBinding(binding: Vp2ExampleFragmentBinding) {
        super.initBinding(binding)
        val pages = listOf(firstPageViewModel, secondPageViewModel)
        viewModel.items.value = pages
        pages.forEach {
            observeNavigation(it)
        }

        setupTabMediator(binding.tabLayout, binding.viewpager2)
    }

    override fun setupViewPagerConfig(binding: Vp2ExampleFragmentBinding) {
        binding.vpConfig = createRecyclerMultiConfig {
            itemId = BR.vm
        }
    }
}