package com.rasalexman.erb.ui.viewpager2example

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.rasalexman.easyrecyclerbinding.createRecyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.MainActivity
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.Vp2ExampleFragmentBinding
import com.rasalexman.erb.ui.base.BasePagerBindingFragment
import com.rasalexman.erb.ui.base.BaseViewModel
import com.rasalexman.erb.ui.viewpager2example.pages.FirstPageViewModel
import com.rasalexman.erb.ui.viewpager2example.pages.SecondPageViewModel

class ViewPager2ExampleFragment :
    BasePagerBindingFragment<Vp2ExampleFragmentBinding, ViewPager2ExampleViewModel>() {
    override val layoutId: Int get() = R.layout.vp2_example_fragment
    override val viewModel: ViewPager2ExampleViewModel by viewModels()
    private var searchViewItem: SearchView? = null
    private var searchMenuItem: MenuItem? = null

    override val pageTitles: List<String> = listOf("Login", "With Recycler")
    override val pagesVMList: List<BaseViewModel> by lazy {
        listOf(firstPageViewModel, secondPageViewModel)
    }

    private val firstPageViewModel: FirstPageViewModel by viewModels()
    private val secondPageViewModel: SecondPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val mainActivity = (requireActivity() as MainActivity)
        val item = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView
        val searchManager = mainActivity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(mainActivity.componentName))
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                secondPageViewModel.onQueryTextChanged(newText.orEmpty())
                return true
            }
        })
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                secondPageViewModel.searchState.postValue(SearchState.OPEN)
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                secondPageViewModel.onQueryTextChanged("")
                secondPageViewModel.searchState.postValue(SearchState.CLOSED)
                return true
            }

        })
        searchMenuItem = item
        searchViewItem = searchView

        if(secondPageViewModel.currentQuery.isNotEmpty()) {
            view?.postDelayed({
                searchMenuItem?.expandActionView()
            }, 500L)
        }
    }

    override fun initBinding(binding: Vp2ExampleFragmentBinding) {
        super.initBinding(binding)
        val pages = listOf(firstPageViewModel, secondPageViewModel)
        viewModel.items.value = pages
        pages.forEach {
            observeNavigation(it)
        }

        setupTabMediator(binding.tabLayout, binding.viewpager2)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        secondPageViewModel.searchState.observe(viewLifecycleOwner) {
            if(it == SearchState.CLOSED) {
                searchViewItem?.setQuery("", false)
                if(searchMenuItem?.isActionViewExpanded == true) {
                    searchMenuItem?.collapseActionView()
                }
            } else if (it == SearchState.OPEN) {
                searchViewItem?.setQuery(secondPageViewModel.currentQuery, false)
            }
        }
    }

    override fun setupViewPagerConfig(binding: Vp2ExampleFragmentBinding) {
        binding.vpConfig = createRecyclerMultiConfig {
            itemId = BR.vm

            onPageSelectedListener = {
                viewModel.onPageSelected(it)
            }
        }
    }

    override fun onDestroyView() {
        searchMenuItem?.setOnActionExpandListener(null)
        searchViewItem?.setOnQueryTextListener(null)
        searchMenuItem = null
        searchViewItem = null
        super.onDestroyView()
    }
}