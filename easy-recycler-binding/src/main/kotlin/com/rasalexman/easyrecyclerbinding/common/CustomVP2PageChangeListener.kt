package com.rasalexman.easyrecyclerbinding.common

import androidx.viewpager2.widget.ViewPager2

open class CustomVP2PageChangeListener : ViewPager2.OnPageChangeCallback() {
    var onPageInverseCallback: (() -> Unit)? = null
    var onPageSelectedCallback: ((Int) -> Unit)? = null
    var onPageScrollStateCallback: ((Int) -> Unit)? = null
    var onPageScrolledCallback: ((Int) -> Unit)? = null

    override fun onPageSelected(position: Int) {
        onPageSelectedCallback?.invoke(position)
        onPageInverseCallback?.invoke()
        super.onPageSelected(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        onPageScrollStateCallback?.invoke(state)
        super.onPageScrollStateChanged(state)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        onPageScrolledCallback?.invoke(position)
        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
    }

    fun clear() {
        onPageSelectedCallback = null
        onPageScrollStateCallback = null
        onPageScrolledCallback = null
        onPageInverseCallback = null
    }
}