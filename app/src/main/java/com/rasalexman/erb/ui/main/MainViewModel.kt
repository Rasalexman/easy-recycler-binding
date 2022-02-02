package com.rasalexman.erb.ui.main

import com.rasalexman.erb.ui.base.BaseViewModel

class MainViewModel : BaseViewModel() {

    fun onVpButtonClicked() {
        navigationState.value = MainFragmentDirections.showViewPagerFragment()
    }

    fun onVp2ButtonClicked() {
        navigationState.value = MainFragmentDirections.showViewPager2Fragment()
    }

    fun onVp2PagingButtonClicked() {
        navigationState.value = MainFragmentDirections.showViewPager2PagingFragment()
    }

    fun onRvButtonClicked() {
        navigationState.value = MainFragmentDirections.showRecyclerViewFragment()
    }

    fun onPagingButtonClicked() {
        navigationState.value = MainFragmentDirections.showPagingFragment()
    }

    fun onHorizontalButtonClicked() {
        navigationState.value = MainFragmentDirections.showHorizontalFragment()
    }
}