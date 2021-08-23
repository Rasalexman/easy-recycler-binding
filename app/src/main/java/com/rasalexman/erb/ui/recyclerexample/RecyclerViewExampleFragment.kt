package com.rasalexman.erb.ui.recyclerexample

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.createRecyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.RvExampleFragmentBinding
import com.rasalexman.erb.ui.base.BaseBindingFragment
import kotlin.random.Random

class RecyclerViewExampleFragment : BaseBindingFragment<RvExampleFragmentBinding, RecyclerViewExampleViewModel>() {
    override val layoutId: Int get() = R.layout.rv_example_fragment
    override val viewModel: RecyclerViewExampleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_remove, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val rand = Random.nextInt(100)
        val isfirst = rand%2 == 0

        if(item.itemId == R.id.action_remove) {
            viewModel.onRemoveClicked(isfirst)
        } else if (item.itemId == R.id.action_add) {
            viewModel.addItems(atFirst = isfirst)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initBinding(binding: RvExampleFragmentBinding) {
        super.initBinding(binding)
        binding.rvConfig = createRecyclerMultiConfig {
            itemId = BR.item

            onItemClick = { item: IBindingModel, pos: Int ->
                println("onItemClick Position = $pos")
                Toast.makeText(context, "onItemClick", Toast.LENGTH_SHORT).show()
                viewModel.onShowSelectedItemFragment(item)
            }
            onItemLongClickListener = { _, pos: Int ->
                println("onItemLongClickListener Position = $pos")
                Toast.makeText(context, "onItemLongClickListener", Toast.LENGTH_SHORT).show()
            }
            onItemDoubleClicked = { _, pos: Int ->
                println("onItemDoubleClicked Position = $pos")
                Toast.makeText(context, "onItemDoubleClicked", Toast.LENGTH_SHORT).show()
            }

            onLoadMore = {
                //viewModel.addItems(atFirst = false)
            }
        }
    }
}