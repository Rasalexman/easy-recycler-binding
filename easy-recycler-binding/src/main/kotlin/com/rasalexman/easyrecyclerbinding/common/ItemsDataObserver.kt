package com.rasalexman.easyrecyclerbinding.common

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rasalexman.easyrecyclerbinding.ScrollPosition
import java.lang.ref.WeakReference

internal class ItemsDataObserver(
    recyclerView: RecyclerView,
    scrollPosition: ScrollPosition?,
    scrollListener: EndlessRecyclerOnScrollListener?
) : RecyclerView.AdapterDataObserver() {
    private val scrollPositionWeakRef = WeakReference(scrollPosition)
    private val recyclerWeakRef = WeakReference(recyclerView)
    private val scrollListenerWeakRef = WeakReference(scrollListener)
    private var isFirstInsert = false

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        resetScrollListenerTotalCount()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (positionStart == 0) {
            isFirstInsert = false
        }
        if(itemCount > 0) {
            resetScrollListenerTotalCount()
        }

        if(!isFirstInsert && itemCount > 0) {
            val scrollIndex = scrollPositionWeakRef.get()?.index ?: 0
            if(scrollIndex > 0) {
                isFirstInsert = true
                changeScrollPosition(itemCount)
            }
        }
    }

    fun clear() {
        scrollPositionWeakRef.clear()
        recyclerWeakRef.clear()
        scrollListenerWeakRef.clear()
    }

    fun changeScrollPosition(itemCount: Int) {
        if (itemCount > 0) {
            scrollPositionWeakRef.get()?.let { scrollPos ->
                if (scrollPos.isNotEmpty()) {
                    recyclerWeakRef.get()?.let { recyclerView ->
                        recyclerView.stopScroll()
                        val linearLayoutManager =
                            (recyclerView.layoutManager as? LinearLayoutManager?)
                        linearLayoutManager?.scrollToPositionWithOffset(
                            scrollPos.index,
                            scrollPos.top
                        )
                    }
                }
            }
        }
    }

    private fun resetScrollListenerTotalCount() {
        scrollListenerWeakRef.get()?.let { scrollListener ->
            val itemsCount = recyclerWeakRef.get()?.adapter?.itemCount ?: 0
            scrollListener.resetTotalCount(itemsCount)
        }
    }
}