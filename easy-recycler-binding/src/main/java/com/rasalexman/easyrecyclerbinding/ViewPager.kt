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


@BindingAdapter(value = ["viewPagerSettings", "tabPosition", "positionAttrChanged"], requireAll = false)
fun setupViewPager(
    viewPager: ViewPager,
    viewPagerSettings: ViewPagerSettings?,
    tabPosition: Int?,
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
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageSelected(position: Int) {
                changeListener?.onChange()
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

/**
 * Настройки динамического ViewPager'а, используется как обычные ViewPagerSettings,
 * только добавляется getDynamicData (LiveData, при изменении значения которой
 * происходит notifyDataSetChanged у адаптера) и lifecycleOwner (для избежания утечек памяти
 * при обсерве liveDat'ы)
 */
interface DynamicViewPagerSettings : ViewPagerSettings {
    /**
     * Получение LifecycleOwner для обсерва лайвдаты
     *
     * @return lifecycleOwner на котором будет обсервиться liveData
     */
    fun getLifecycleOwner(): LifecycleOwner

    /**
     * LiveData, которую слушает adapter, при изменении данных в которой происходит
     * notifyDataSetChanged
     */
    fun getDynamicData(): LiveData<*>
}

internal open class ViewPagerAdapter(private val viewPagerSettings: ViewPagerSettings) : PagerAdapter() {

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

/**
 * Динамический адаптер viewPager'a, для облегчения изменения количества страниц в viewPager,
 * расширяет дефолтный ViewPagerAdapter, позволяет вызывать обновление страниц pager'а, при
 * изменении значения liveDat'ы, переданной в viewPagerSettings
 *
 * @param viewPagerSettings - настройки динамического viewPager'а
 * @see DynamicViewPagerAdapter
 */
internal class DynamicViewPagerAdapter(
    viewPagerSettings: DynamicViewPagerSettings
) : ViewPagerAdapter(viewPagerSettings) {
    init {
        viewPagerSettings.getDynamicData().observe(viewPagerSettings.getLifecycleOwner(), Observer {
            notifyDataSetChanged()
        })
    }
}