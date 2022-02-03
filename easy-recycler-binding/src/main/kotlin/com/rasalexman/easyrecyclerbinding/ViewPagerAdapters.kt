@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")

package com.rasalexman.easyrecyclerbinding

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.viewpager.widget.ViewPager
import com.rasalexman.easyrecyclerbinding.adapters.DynamicViewPagerAdapter
import com.rasalexman.easyrecyclerbinding.adapters.ViewPagerAdapter
import com.rasalexman.easyrecyclerbinding.common.CustomVPPageChangeListener


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
            val pagesScreenOffset = viewPagerSettings.getDefaultScreenOffset()
            offscreenPageLimit = when {
                pagesScreenOffset == DEFAULT_CONFIG_PAGES_OFFSET -> viewPagerSettings.countTab()
                pagesScreenOffset >= 0 -> pagesScreenOffset
                else -> DEFAULT_PAGES_OFFSET
            }

            tabPosition?.let {
                if (it != currentItem) {
                    setCurrentItem(it, false)
                }
                pageSelectionListener?.onPageSelected(it)
            }
        }

        val pageChangeListener = CustomVPPageChangeListener { position ->
            changeListener?.onChange()
            pageSelectionListener?.onPageSelected(position)
        }
        viewPager.addOnPageChangeListener(pageChangeListener)
        viewPager.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View?) = Unit
            override fun onViewDetachedFromWindow(p0: View?) {
                pageChangeListener.clear()
                viewPager.removeOnPageChangeListener(pageChangeListener)
                viewPager.removeOnAttachStateChangeListener(this)
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
    fun getDefaultScreenOffset(): Int
}

private const val DEFAULT_CONFIG_PAGES_OFFSET = -1
private const val DEFAULT_PAGES_OFFSET = 1

open class ViewPagerConfig(
    private val pageCreator: (container: ViewGroup, position: Int) -> View,
    private val titleCreator: (Int) -> String,
    private val tabCount: Int,
    private val defaultOffscreenPages: Int = DEFAULT_CONFIG_PAGES_OFFSET
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

    override fun getDefaultScreenOffset(): Int {
        return defaultOffscreenPages
    }

    class ViewPagerConfigBuilder {
        var pageCreator: ((container: ViewGroup, position: Int) -> View)? = null
        var titleCreator: ((Int) -> String)? = null
        var tabCount: Int? = null
        var defaultOffscreenPages: Int = DEFAULT_CONFIG_PAGES_OFFSET

        fun build(): ViewPagerSettings {
            return ViewPagerConfig(
                pageCreator
                    ?: throw NullPointerException("ViewPagerSettings::pageCreator must not be null"),
                titleCreator
                    ?: throw NullPointerException("ViewPagerSettings::titleCreator must not be null"),
                tabCount
                    ?: throw NullPointerException("ViewPagerSettings::tabCount must not be null"),
                defaultOffscreenPages = defaultOffscreenPages
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

    override fun getDefaultScreenOffset(): Int {
        return DEFAULT_CONFIG_PAGES_OFFSET
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

interface PageSelectionListener {
    fun onPageSelected(position: Int)
}