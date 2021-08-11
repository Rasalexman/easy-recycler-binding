package com.rasalexman.erb.ui.paging

import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.createStateAdapter
import com.rasalexman.easyrecyclerbinding.createPagingRecyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.RvPagingFragmentBinding
import com.rasalexman.erb.models.LoadingItem
import com.rasalexman.erb.ui.base.BaseBindingFragment

class PagingFragment : BaseBindingFragment<RvPagingFragmentBinding, PagingViewModel>() {
    override val layoutId: Int
        get() = R.layout.rv_paging_fragment

    override val viewModel: PagingViewModel by viewModels()

    override fun initBinding(binding: RvPagingFragmentBinding) {
        super.initBinding(binding)
        binding.rvConfig = createPagingRecyclerMultiConfig {
            itemId = BR.item

            onItemClick = { item, _ ->
                viewModel.onShowSelectedItemFragment(item)
            }

            stateFooterAdapter = createStateAdapter(LoadingItem()) {
                itemId = BR.item
            }
            /*stateHeaderAdapter = createStateAdapter(LoadingItem()) {
                itemId = BR.item
            }*/
        }
    }
}