package com.rasalexman.easyrecyclerbinding.adapters

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.rasalexman.easyrecyclerbinding.ViewPagerSettings

internal open class ViewPagerAdapter(
    private val viewPagerSettings: ViewPagerSettings
) : PagerAdapter() {

    override fun getCount(): Int = viewPagerSettings.countTab()

    override fun isViewFromObject(view: View, other: Any): Boolean {
        return view === other
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = viewPagerSettings.getPagerItemView(container, position)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return viewPagerSettings.getTextTitle(position)
    }
}