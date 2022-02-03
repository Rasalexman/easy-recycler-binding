package com.rasalexman.erb.ui.viewpagerexample

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.DiffCallback
import com.rasalexman.easyrecyclerbinding.createBindingWithViewModel
import com.rasalexman.easyrecyclerbinding.createRecyclerConfig
import com.rasalexman.easyrecyclerbinding.viewPagerConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.*
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.RecyclerItemUI
import com.rasalexman.erb.models.RecyclerItemUI2
import com.rasalexman.erb.ui.base.BaseBindingFragment

class ViewPagerExampleFragment : BaseBindingFragment<VpExampleFragmentBinding, ViewPagerExampleViewModel>() {
    override val layoutId: Int get() = R.layout.vp_example_fragment
    override val viewModel: ViewPagerExampleViewModel by viewModels()

    private val pageTitles: Array<String> by lazy {
        requireContext().resources.getStringArray(R.array.vp_titles)
    }

    private val firstItemsDiffUtil: DiffCallback<IRecyclerItem> by lazy {
        object : DiffCallback<IRecyclerItem>() {
            override fun areContentsTheSame(
                oldItem: IRecyclerItem,
                newItem: IRecyclerItem
            ): Boolean {
                return oldItem.id == newItem.id && oldItem.isChecked.get() == newItem.isChecked.get()
            }
        }
    }

    private val secondItemsDiffUtil: DiffCallback<IRecyclerItem> by lazy {
        object : DiffCallback<IRecyclerItem>() {
            override fun areContentsTheSame(
                oldItem: IRecyclerItem,
                newItem: IRecyclerItem
            ): Boolean {
                return oldItem.id == newItem.id && oldItem.isChecked.get() == newItem.isChecked.get()
            }
        }
    }

    override fun initBinding(binding: VpExampleFragmentBinding) {
        super.initBinding(binding)
        binding.viewPagerSettings = viewPagerConfig {
            pageCreator = ::viewPagerItemsCreator
            titleCreator = ::viewPagerTitleCreator
            tabCount = pageTitles.size
            defaultOffscreenPages = 0
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
                    diffUtilCallback = firstItemsDiffUtil

                    onItemClick = { item, _ ->
                        viewModel.onShowSelectedRecyclerItemFragment(item)
                    }
                }
            }
            else -> createBindingWithViewModel<ItemVpSecondPageBinding, ViewPagerExampleViewModel>(
                layoutId = R.layout.item_vp_second_page,
                container = container,
                viewModel = viewModel,
                viewModelBRId = BR.vm
            ).apply {
                rvConfig = createRecyclerConfig<RecyclerItemUI2, ItemRecycler2Binding> {
                    layoutId = R.layout.item_recycler2
                    itemId = BR.item
                    diffUtilCallback = secondItemsDiffUtil
                }
            }
        }.root
    }

    private fun viewPagerTitleCreator(position: Int): String {
        return pageTitles.getOrNull(position).orEmpty()
    }

    companion object {
        private const val PAGE_FIRST = 0
    }
}