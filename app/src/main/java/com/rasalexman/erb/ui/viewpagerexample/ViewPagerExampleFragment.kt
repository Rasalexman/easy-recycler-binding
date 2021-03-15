package com.rasalexman.erb.ui.viewpagerexample

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.createBindingWithViewModel
import com.rasalexman.easyrecyclerbinding.createRecyclerConfig
import com.rasalexman.easyrecyclerbinding.viewPagerConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.ItemRecyclerBinding
import com.rasalexman.erb.databinding.ItemVpFirstPageBinding
import com.rasalexman.erb.databinding.ItemVpSecondPageBinding
import com.rasalexman.erb.databinding.VpExampleFragmentBinding
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.ui.base.BaseBindingFragment

class ViewPagerExampleFragment : BaseBindingFragment<VpExampleFragmentBinding, ViewPagerExampleViewModel>() {
    override val layoutId: Int get() = R.layout.vp_example_fragment
    override val viewModel: ViewPagerExampleViewModel by viewModels()

    private val pageTitles: Array<String> by lazy {
        requireContext().resources.getStringArray(R.array.vp_titles)
    }

    override fun initBinding(binding: VpExampleFragmentBinding) {
        super.initBinding(binding)
        binding.viewPagerSettings = viewPagerConfig {
            pageCreator = ::viewPagerItemsCreator
            titleCreator = ::viewPagerTitleCreator
            tabCount = pageTitles.size
        }
    }

    private fun viewPagerItemsCreator(container: ViewGroup, position: Int): View {
        return when (position) {
            PAGE_FIRST -> createBindingWithViewModel<ItemVpFirstPageBinding, ViewPagerExampleViewModel>(
                layoutId = R.layout.item_vp_first_page,
                container = container,
                viewModel = viewModel,
                viewModelBRId = BR.vm
            ).apply {
                rvConfig = createRecyclerConfig<RecyclerItemUI, ItemRecyclerBinding> {
                    layoutId = R.layout.item_recycler
                    itemId = BR.item
                }
            }
            else -> createBindingWithViewModel<ItemVpSecondPageBinding, ViewPagerExampleViewModel>(
                layoutId = R.layout.item_vp_second_page,
                container = container,
                viewModel = viewModel,
                viewModelBRId = BR.vm
            ).apply {
                rvConfig = createRecyclerConfig<RecyclerItemUI, ItemRecyclerBinding> {
                    layoutId = R.layout.item_recycler
                    itemId = BR.item
                }
            }
        }.root
    }

    private fun viewPagerTitleCreator(position: Int): String {
        return pageTitles.getOrNull(position).orEmpty()
    }

    companion object {
        private const val PAGE_FIRST = 0
        private const val PAGE_SECOND = 1
    }
}