package com.rasalexman.easyrecyclerbinding.common

import androidx.viewpager2.widget.ViewPager2

open class CustomPageChangeCallback : ViewPager2.OnPageChangeCallback() {
    var onPageSelectedCallback: ((Int) -> Unit)? = null
    var onPageScrollStateCallback: ((Int) -> Unit)? = null
    var onPageScrolledCallback: ((Int) -> Unit)? = null

    override fun onPageSelected(position: Int) {
        onPageSelectedCallback?.invoke(position)
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
}