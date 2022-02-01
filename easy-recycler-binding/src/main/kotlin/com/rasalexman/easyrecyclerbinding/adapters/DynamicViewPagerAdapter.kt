package com.rasalexman.easyrecyclerbinding.adapters

import com.rasalexman.easyrecyclerbinding.DynamicViewPagerSettings

internal class DynamicViewPagerAdapter(
    viewPagerSettings: DynamicViewPagerSettings
) : ViewPagerAdapter(viewPagerSettings) {
    init {
        viewPagerSettings.getDynamicData().observe(viewPagerSettings.getLifecycleOwner()) {
            notifyDataSetChanged()
        }
    }
}