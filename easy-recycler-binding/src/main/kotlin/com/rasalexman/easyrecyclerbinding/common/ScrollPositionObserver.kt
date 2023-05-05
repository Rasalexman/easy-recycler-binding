package com.rasalexman.easyrecyclerbinding.common

import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rasalexman.easyrecyclerbinding.ScrollPosition
import com.rasalexman.easyrecyclerbinding.clearAdapter
import java.lang.ref.WeakReference

@Suppress("unused")
internal class ScrollPositionObserver(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    scrollPosition: ScrollPosition?,
    scrollListener: EndlessRecyclerOnScrollListener?
) : DefaultLifecycleObserver {

    private val ownerWeakRef = WeakReference(lifecycleOwner)
    private val scrollPositionWeakRef = WeakReference(scrollPosition)
    private val recyclerWeakRef = WeakReference(recyclerView)

    private var adapterDataObserver: ItemsDataObserver? = null

    init {
        if (scrollPosition != null || scrollListener != null) {
            adapterDataObserver =
                ItemsDataObserver(recyclerView, scrollPosition, scrollListener).also {
                    recyclerView.adapter?.registerAdapterDataObserver(it)
                }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        saveScrollPosition()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        val resumedItemsCount = recyclerWeakRef.get()?.adapter?.itemCount ?: 0
        adapterDataObserver?.changeScrollPosition(resumedItemsCount)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        clearWhenViewDestroy()
    }

    private fun saveScrollPosition() {
        scrollPositionWeakRef.get()?.let { scrollPosition ->
            recyclerWeakRef.get()?.let { currentRV ->
                currentRV.stopScroll()
                val linearLayoutManager = (currentRV.layoutManager as? LinearLayoutManager?)
                val isHorizontal = linearLayoutManager?.orientation == LinearLayout.HORIZONTAL

                val position = if (isHorizontal) {
                    currentRV.getChildAt(0)?.let { it.left - currentRV.paddingStart } ?: 0
                } else {
                    currentRV.getChildAt(0)?.let { it.top - currentRV.paddingTop } ?: 0
                }

                scrollPosition.apply {
                    index = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
                    top = position
                }
            }
        }
    }


    private fun clearWhenViewDestroy() {
        scrollPositionWeakRef.clear()
        recyclerWeakRef.get()?.let { rv ->
            rv.clearOnScrollListeners()
            adapterDataObserver?.let { dataObserver ->
                rv.adapter?.unregisterAdapterDataObserver(dataObserver)
                dataObserver.clear()
            }
            rv.adapter?.clearAdapter<Any, ViewDataBinding>()
            rv.adapter = null
        }
        recyclerWeakRef.clear()
        ownerWeakRef.get()?.lifecycle?.removeObserver(this)
        ownerWeakRef.clear()
        adapterDataObserver = null
    }
}