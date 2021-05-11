package com.rasalexman.easyrecyclerbinding

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


@BindingAdapter(
    value = ["vp_settings", "tabPosition", "pageSelectionListener", "positionAttrChanged"],
    requireAll = false
)
fun setupViewPager(
    viewPager: ViewPager,
    viewPagerSettings: ViewPagerSettings?,
    tabPosition: Int?,
    pageSelectionListener: PageSelectionListener?,
    changeListener: InverseBindingListener?
) {

    if (viewPagerSettings == null) {
        return
    }

    if (viewPager.adapter == null) {
        viewPager.apply {
            adapter = getViewPagerAdapter(viewPagerSettings)
            offscreenPageLimit = viewPagerSettings.countTab()
            tabPosition?.let {
                if (it != currentItem) {
                    setCurrentItem(it, false)
                }
            }
        }
        viewPager.clearOnPageChangeListeners()
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit

            override fun onPageSelected(position: Int) {
                changeListener?.onChange()
                pageSelectionListener?.onPageSelected(position)
            }
        })

    } else {
        tabPosition?.let {
            if (it != viewPager.currentItem) {
                viewPager.setCurrentItem(it, false)
            }
        }
    }
}

@InverseBindingAdapter(attribute = "tabPosition", event = "positionAttrChanged")
fun getSelectedPosition(viewPager: ViewPager): Int {
    return viewPager.currentItem
}

/**
 * Получение адаптера, в зависимости яляется ли viewPagerSettings настройками динамического
 * адаптера или обычного
 */
private fun getViewPagerAdapter(viewPagerSettings: ViewPagerSettings): ViewPagerAdapter {
    return if (viewPagerSettings is DynamicViewPagerSettings) {
        DynamicViewPagerAdapter(viewPagerSettings)
    } else {
        ViewPagerAdapter(viewPagerSettings)
    }
}

interface ViewPagerSettings {
    fun getPagerItemView(container: ViewGroup, position: Int): View
    fun getTextTitle(position: Int): String
    fun countTab(): Int
}

open class ViewPagerConfig(
    private val pageCreator: (container: ViewGroup, position: Int) -> View,
    private val titleCreator: (Int) -> String,
    private val tabCount: Int
) : ViewPagerSettings {
    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return pageCreator.invoke(container, position)
    }

    override fun getTextTitle(position: Int): String {
        return titleCreator.invoke(position)
    }

    override fun countTab(): Int {
        return tabCount
    }

    class ViewPagerConfigBuilder {
        var pageCreator: ((container: ViewGroup, position: Int) -> View)? = null
        var titleCreator: ((Int) -> String)? = null
        var tabCount: Int? = null

        fun build(): ViewPagerSettings {
            return ViewPagerConfig(
                pageCreator
                    ?: throw NullPointerException("ViewPagerSettings::pageCreator must not be null"),
                titleCreator
                    ?: throw NullPointerException("ViewPagerSettings::titleCreator must not be null"),
                tabCount
                    ?: throw NullPointerException("ViewPagerSettings::tabCount must not be null")
            )
        }
    }
}

fun viewPagerConfig(
    block: ViewPagerConfig.ViewPagerConfigBuilder.() -> Unit
): ViewPagerSettings {
    return ViewPagerConfig.ViewPagerConfigBuilder().apply(block).build()
}

open class DynamicViewPagerConfig(
    private val pageCreator: (container: ViewGroup, position: Int) -> View,
    private val titleCreator: (Int) -> String,
    private val tabCount: Int,
    private val lifecycleOwner: LifecycleOwner,
    private val dynamicData: LiveData<*>
) : DynamicViewPagerSettings {
    override fun getLifecycleOwner(): LifecycleOwner {
        return lifecycleOwner
    }

    override fun getDynamicData(): LiveData<*> {
        return dynamicData
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return pageCreator.invoke(container, position)
    }

    override fun getTextTitle(position: Int): String {
        return titleCreator.invoke(position)
    }

    override fun countTab(): Int {
        return tabCount
    }

    class DynamicViewPagerConfigBuilder {
        var pageCreator: ((container: ViewGroup, position: Int) -> View)? = null
        var titleCreator: ((Int) -> String)? = null
        var tabCount: Int? = null
        var lifecycleOwner: LifecycleOwner? = null
        var dynamicData: LiveData<*>? = null

        fun build(): DynamicViewPagerSettings {
            return DynamicViewPagerConfig(
                pageCreator = pageCreator
                    ?: throw NullPointerException("ViewPagerSettings::pageCreator must not be null"),
                titleCreator = titleCreator
                    ?: throw NullPointerException("ViewPagerSettings::titleCreator must not be null"),
                tabCount = tabCount
                    ?: throw NullPointerException("ViewPagerSettings::tabCount must not be null"),
                lifecycleOwner = lifecycleOwner
                    ?: throw NullPointerException("ViewPagerSettings::lifecycleOwner must not be null"),
                dynamicData = dynamicData
                    ?: throw NullPointerException("ViewPagerSettings::dynamicData must not be null")
            )
        }
    }
}


fun dynamicViewPagerConfig(
    block: DynamicViewPagerConfig.DynamicViewPagerConfigBuilder.() -> Unit
): DynamicViewPagerSettings {
    return DynamicViewPagerConfig.DynamicViewPagerConfigBuilder().apply(block).build()
}

interface DynamicViewPagerSettings : ViewPagerSettings {
    fun getLifecycleOwner(): LifecycleOwner
    fun getDynamicData(): LiveData<*>
}

internal open class ViewPagerAdapter(
    private val viewPagerSettings: ViewPagerSettings
    ) : PagerAdapter() {

    override fun getCount(): Int = viewPagerSettings.countTab()

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
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

internal class DynamicViewPagerAdapter(
    viewPagerSettings: DynamicViewPagerSettings
) : ViewPagerAdapter(viewPagerSettings) {
    init {
        viewPagerSettings.getDynamicData().observe(viewPagerSettings.getLifecycleOwner(), Observer {
            notifyDataSetChanged()
        })
    }
}

interface PageSelectionListener {
    fun onPageSelected(position: Int)
}