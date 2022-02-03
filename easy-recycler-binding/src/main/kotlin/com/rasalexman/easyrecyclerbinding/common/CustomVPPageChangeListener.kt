package com.rasalexman.easyrecyclerbinding.common

import androidx.viewpager.widget.ViewPager

class CustomVPPageChangeListener(
    var onPageSelectedCallback: ((Int)->Unit)? = null
) : ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) = Unit
    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) = Unit

    override fun onPageSelected(position: Int) {
        onPageSelectedCallback?.invoke(position)
    }

    fun clear() {
        onPageSelectedCallback = null
    }
}