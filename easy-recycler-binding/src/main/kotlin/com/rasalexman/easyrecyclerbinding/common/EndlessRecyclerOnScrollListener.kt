@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate", "SameParameterValue")
package com.rasalexman.easyrecyclerbinding.common

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

open class EndlessRecyclerOnScrollListener : RecyclerView.OnScrollListener {

    private var enabled = true
    private var previousTotal = 0
    private var isLoading = true
    private var visibleThreshold = RecyclerView.NO_POSITION
    var firstVisibleItem: Int = 0
        private set
    var visibleItemCount: Int = 0
        private set
    var totalItemCount: Int = 0
        private set

    private var isOrientationHelperVertical: Boolean = false
    private var orientationHelper: OrientationHelper? = null
    private var onScrollListener: ((Int) -> Unit)? = null

    var currentPage = 0
        private set

    lateinit var layoutManager: RecyclerView.LayoutManager
        private set

    constructor()

    constructor(layoutManager: RecyclerView.LayoutManager) {
        this.layoutManager = layoutManager
    }

    constructor(visibleThreshold: Int, listener: ((Int)->Unit)? = null) {
        this.visibleThreshold = visibleThreshold
        this.onScrollListener = listener
    }

    constructor(layoutManager: RecyclerView.LayoutManager, visibleThreshold: Int, listener: ((Int)->Unit)? = null) {
        this.layoutManager = layoutManager
        this.visibleThreshold = visibleThreshold
        this.onScrollListener = listener
    }

    private fun findFirstVisibleItemPosition(recyclerView: RecyclerView): Int {
        return findVisibleChildIndexFrom(
            recyclerView = recyclerView,
            fromIndex = 0,
            toIndex = layoutManager.childCount
        )
    }

    private fun findLastVisibleItemPosition(recyclerView: RecyclerView): Int {
        return findVisibleChildIndexFrom(
            recyclerView = recyclerView,
            fromIndex = recyclerView.childCount - 1,
            toIndex = -1
        )
    }

    private fun findVisibleChildIndexFrom(recyclerView: RecyclerView, fromIndex: Int, toIndex: Int): Int {
        val child = findOneVisibleChild(
            fromIndex = fromIndex,
            toIndex = toIndex,
            completelyVisible = false,
            acceptPartiallyVisible = true
        )
        return if (child == null) {
            RecyclerView.NO_POSITION
        } else {
            recyclerView.getChildAdapterPosition(child)
        }
    }

    private fun findOneVisibleChild(
        fromIndex: Int,
        toIndex: Int,
        completelyVisible: Boolean,
        acceptPartiallyVisible: Boolean
    ): View? {
        if (layoutManager.canScrollVertically() != isOrientationHelperVertical || orientationHelper == null) {
            isOrientationHelperVertical = layoutManager.canScrollVertically()
            orientationHelper = if (isOrientationHelperVertical)
                OrientationHelper.createVerticalHelper(layoutManager)
            else
                OrientationHelper.createHorizontalHelper(layoutManager)
        }

        val mOrientationHelper = this.orientationHelper ?: return null

        val start = mOrientationHelper.startAfterPadding
        val end = mOrientationHelper.endAfterPadding
        val next = if (toIndex > fromIndex) 1 else -1
        var partiallyVisible: View? = null
        var i = fromIndex
        while (i != toIndex) {
            val child = layoutManager.getChildAt(i)
            if (child != null) {
                val childStart = mOrientationHelper.getDecoratedStart(child)
                val childEnd = mOrientationHelper.getDecoratedEnd(child)
                if (childStart < end && childEnd > start) {
                    if (completelyVisible) {
                        if (childStart >= start && childEnd <= end) {
                            return child
                        } else if (acceptPartiallyVisible && partiallyVisible == null) {
                            partiallyVisible = child
                        }
                    } else {
                        return child
                    }
                }
            }
            i += next
        }
        return partiallyVisible
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (enabled) {
            if (!::layoutManager.isInitialized) {
                layoutManager = recyclerView.layoutManager
                    ?: throw RuntimeException("A LayoutManager is required")
            }

            if (visibleThreshold == RecyclerView.NO_POSITION) {
                visibleThreshold =
                    findLastVisibleItemPosition(recyclerView) - findFirstVisibleItemPosition(
                        recyclerView
                    )
            }

            visibleItemCount = recyclerView.childCount
            totalItemCount = layoutManager.itemCount
            firstVisibleItem = findFirstVisibleItemPosition(recyclerView)

            if (isLoading) {
                if (totalItemCount > previousTotal) {
                    isLoading = false
                    previousTotal = totalItemCount
                }
            }
            if (!isLoading) {
                val topPad = (totalItemCount - visibleItemCount)
                val botPad = (firstVisibleItem + visibleThreshold)
                if (topPad <= botPad) {
                    currentPage++
                    onScrollListener?.invoke(currentPage)
                    isLoading = true
                }
            }
        }
    }

    fun enable(): EndlessRecyclerOnScrollListener {
        enabled = true
        return this
    }

    fun disable(): EndlessRecyclerOnScrollListener {
        enabled = false
        return this
    }

    @JvmOverloads
    fun resetPageCount(page: Int = 0) {
        previousTotal = 0
        isLoading = false
        currentPage = page
    }

    fun resetTotalCount(total: Int) {
        //println("----> previous = $previousTotal | total = $total")
        if (previousTotal != 0 && previousTotal != total) {
            previousTotal = total
            isLoading = total > visibleThreshold
        }
    }

    //abstract fun onLoadMore(currentPage: Int)
}