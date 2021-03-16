package com.rasalexman.erb.ui.recyclerexample

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.createRecyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.RvExampleFragmentBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment

class RecyclerViewExampleFragment : BaseBindingFragment<RvExampleFragmentBinding, RecyclerViewExampleViewModel>() {
    override val layoutId: Int get() = R.layout.rv_example_fragment
    override val viewModel: RecyclerViewExampleViewModel by viewModels()

    override fun initBinding(binding: RvExampleFragmentBinding) {
        super.initBinding(binding)
        binding.rvConfig = createRecyclerMultiConfig {
            layoutId = R.layout.item_recycler
            itemId = BR.item
            onItemClick = { item: IBindingModel, pos: Int ->
                Log.d("ITEM_POSITION", "Position = $pos")
                Toast.makeText(context, item.toString(), Toast.LENGTH_SHORT).show()
            }
            onLoadMore = {
                viewModel.createItems()
            }
        }
    }
}