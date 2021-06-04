package com.rasalexman.erb.ui.base

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

abstract class BasePagerBindingFragment<B : ViewDataBinding, VM : BasePagesViewModel> :
    BaseBindingFragment<B, VM>() {

    abstract val pageTitles: List<String>

    private var tabLayoutMediator: TabLayoutMediator? = null
    abstract val pagesVMList: List<BaseViewModel>

    override fun initBinding(binding: B) {
        super.initBinding(binding)
        setupViewPagerConfig(binding)
    }

    abstract fun setupViewPagerConfig(binding: B)

    protected open fun setupTabMediator(tabLayout: TabLayout, viewPager: ViewPager2) {
        tabLayoutMediator =
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setText(pageTitles.getOrNull(position).orEmpty())
            }
    }

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        attachViewPagerMediator()
    }

    private fun attachViewPagerMediator() {
        tabLayoutMediator?.let {
            if (!it.isAttached) {
                it.attach()
            }
        }
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        super.onDestroyView()
    }
}