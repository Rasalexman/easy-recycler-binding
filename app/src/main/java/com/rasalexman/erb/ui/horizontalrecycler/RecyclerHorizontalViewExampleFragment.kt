package com.rasalexman.erb.ui.horizontalrecycler

import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.recyclerConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.ItemTopBinding
import com.rasalexman.erb.databinding.RvExampleFragmentBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment

class RecyclerHorizontalViewExampleFragment :
    BaseBindingFragment<RvExampleFragmentBinding, RecyclerHorizontalViewExampleViewModel>() {
    override val layoutId: Int get() = R.layout.rv_example_fragment
    override val viewModel: RecyclerHorizontalViewExampleViewModel by viewModels()

    override fun initBinding(binding: RvExampleFragmentBinding) {
        super.initBinding(binding)
        binding.rvConfig = recyclerConfig<TopItemUI, ItemTopBinding> {
            itemId = BR.item
            layoutId = R.layout.item_top
            orientation = LinearLayout.HORIZONTAL

            onItemPosClick = { item: TopItemUI, pos: Int ->
                println("onItemClick Position = $pos")
                Toast.makeText(context, "onItemClick", Toast.LENGTH_SHORT).show()
                viewModel.onShowSelectedItemFragment(item)
            }
        }
    }
}