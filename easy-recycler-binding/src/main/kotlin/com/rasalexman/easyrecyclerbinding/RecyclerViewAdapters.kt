@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")

package com.rasalexman.easyrecyclerbinding

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.rasalexman.easyrecyclerbinding.adapters.*
import com.rasalexman.easyrecyclerbinding.common.BindingAdapterType
import com.rasalexman.easyrecyclerbinding.common.CustomVP2PageChangeListener
import com.rasalexman.easyrecyclerbinding.common.EndlessRecyclerOnScrollListener
import com.rasalexman.easyrecyclerbinding.common.ScrollPositionObserver
import java.util.*
import kotlin.math.abs

val changeCallbackMap = WeakHashMap<String, CustomVP2PageChangeListener>(3)

@BindingAdapter(
    value = ["items", "vp_config"],
    requireAll = false
)
fun <ItemType : Any, BindingType : ViewDataBinding> setupViewPager2(
    viewPager: ViewPager2,
    newItems: List<ItemType>?,
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?
) {
    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    val oldItems: MutableList<ItemType> =
        viewPager.getOrCreateOldItems(dataBindingRecyclerViewConfig)

    if (viewPager.adapter == null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewPager.defaultFocusHighlightEnabled = false
        }
        // create adapter for vp
        val adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems)
        // set current adapter
        viewPager.adapter = adapter
        // invoke onAdapterAdded callback
        dataBindingRecyclerViewConfig.onAdapterAdded?.invoke(adapter)


        val callbackKey = viewPager.hashCode().toString()
        val lastCallback = changeCallbackMap.getOrPut(callbackKey) {
            CustomVP2PageChangeListener().apply {
                onPageScrolledCallback = dataBindingRecyclerViewConfig.onScrollListener
                onPageScrollStateCallback = dataBindingRecyclerViewConfig.onPageScrollStateListener
                onPageSelectedCallback = dataBindingRecyclerViewConfig.onPageSelectedListener
            }
        }
        viewPager.registerOnPageChangeCallback(lastCallback)
        viewPager.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View?) = Unit
            override fun onViewDetachedFromWindow(p0: View?) {
                viewPager.unregisterOnPageChangeCallback(lastCallback)
                changeCallbackMap.remove(callbackKey)?.clear()
                viewPager.removeOnAttachStateChangeListener(this)
            }
        })
    }

    viewPager.adapter?.applyAdapterData(
        oldItems = oldItems,
        newItems = newItems,
        rvConfig = dataBindingRecyclerViewConfig
    )
}

@BindingAdapter(value = ["selectedPage", "positionAttrChanged"], requireAll = false)
fun setSelectedPosition(
    viewPager: ViewPager2,
    selectedPage: Int?,
    changeListener: InverseBindingListener?
) {
    val currentPage = selectedPage ?: 0
    val lastPage = viewPager.currentItem
    if (currentPage != lastPage && currentPage >= 0) {
        viewPager.setCurrentItem(currentPage, false)
    }

    val callbackKey = viewPager.hashCode().toString()
    val pageChangeCallback = changeCallbackMap[callbackKey]
    pageChangeCallback?.onPageInverseCallback = {
        changeListener?.onChange()
    }
}

@InverseBindingAdapter(attribute = "selectedPage", event = "positionAttrChanged")
fun getSelectedPosition(viewPager: ViewPager2): Int {
    return viewPager.currentItem
}

@BindingAdapter(
    value = ["items", "rv_config", "position", "pages", "visibleThreshold"],
    requireAll = false
)
fun <ItemType : Any, BindingType : ViewDataBinding> setupRecyclerView(
    recyclerView: RecyclerView,
    newItems: List<ItemType>? = null,
    recyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?,
    scrollPosition: ScrollPosition? = null,
    pagingData: PagingData<ItemType>? = null,
    visibleThreshold: Int = RecyclerView.NO_POSITION
) {

    if (recyclerViewConfig == null) {
        return
    }

    val oldItems: MutableList<ItemType> =
        recyclerView.getOrCreateOldItems(recyclerViewConfig)

    if (recyclerView.adapter == null) {
        val isStandardAdapter =
            recyclerViewConfig.adapterType == BindingAdapterType.STANDARD
        recyclerView.setHasFixedSize(recyclerViewConfig.hasFixedSize)

        var scrollListener: EndlessRecyclerOnScrollListener? = null
        var currentScrollListener: RecyclerView.OnScrollListener? = null
        if (recyclerView.layoutManager == null) {
            val mLayoutManager: RecyclerView.LayoutManager =
                recyclerViewConfig.layoutManager
                    ?: LinearLayoutManager(
                        recyclerView.context,
                        recyclerViewConfig.orientation,
                        recyclerViewConfig.isReverseLayout
                    )
            recyclerView.layoutManager = mLayoutManager

            if (isStandardAdapter) {
                // first clear all listeners
                recyclerView.clearOnScrollListeners()
                // custom listener or check another scroll listener scroll listener
                currentScrollListener = recyclerViewConfig.onScrollListener?.let { onLoadMoreHandler ->
                        EndlessRecyclerOnScrollListener(
                            mLayoutManager,
                            visibleThreshold,
                            onLoadMoreHandler
                        ).also {
                            scrollListener = it
                        }
                    } ?: recyclerViewConfig.recyclerOnScrollListener
                // add scroll listener
                currentScrollListener?.let(recyclerView::addOnScrollListener)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.touchscreenBlocksFocus = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recyclerView.defaultFocusHighlightEnabled = false
        }

        // create adapter
        val adapter = recyclerViewConfig.createAdapter(oldItems)
        // choose method to apply adapter to RV
        if(recyclerViewConfig.isSwapAdapter) {
            recyclerView.swapAdapter(adapter, recyclerViewConfig.removeAndRecycleExistingViews)
        } else {
            recyclerView.adapter = adapter
        }

        // invoke onAdapterAdded callback
        recyclerViewConfig.onAdapterAdded?.invoke(adapter)
        // add scroll position saver
        val currentLifecycleOwner = recyclerViewConfig.lifecycleOwner
        if(currentLifecycleOwner == null) {
            recyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(rv: View?) {
                    if (rv is RecyclerView) {
                        addLifecycleObserver(
                            rv, currentLifecycleOwner,
                            scrollPosition, scrollListener
                        )
                    }
                }

                override fun onViewDetachedFromWindow(rv: View?) {
                    if (rv is RecyclerView) {
                        currentScrollListener?.let(rv::removeOnScrollListener)
                        rv.removeOnAttachStateChangeListener(this)
                    }
                }
            })
        } else {
            addLifecycleObserver(
                recyclerView, currentLifecycleOwner,
                scrollPosition, scrollListener
            )
        }

        recyclerViewConfig.itemAnimator?.let {
            recyclerView.itemAnimator = it
        }

        recyclerViewConfig.itemDecorator?.let { decorationList ->
            repeat(recyclerView.itemDecorationCount) {
                recyclerView.removeItemDecorationAt(it)
            }
            decorationList.forEach {
                recyclerView.addItemDecoration(it)
            }
        }
    }

    recyclerView.adapter?.applyAdapterData(
        oldItems = oldItems,
        newItems = newItems,
        pagingData = pagingData,
        rvConfig = recyclerViewConfig
    )
}

private fun addLifecycleObserver(
    recyclerView: RecyclerView,
    lcOwner: LifecycleOwner?,
    scrollPosition: ScrollPosition?,
    scrollListener: EndlessRecyclerOnScrollListener?
) {
// create scroll position observer and scroll listener recalculation
    val lifecycleOwner = try {
        if (lcOwner != null) {
            lcOwner
        } else {
            val parentFragment = recyclerView.findFragment<Fragment>()
            parentFragment.viewLifecycleOwner
        }
    } catch (e: Exception) {
        //println("[ERROR]: error with recyclerView.findFragment = $e")
        recyclerView.context.run {
            val primaryFragment = findPrimaryFragment()
            primaryFragment?.viewLifecycleOwner ?: getOwner<LifecycleOwner>()
        }
    }

    lifecycleOwner?.apply {
        lifecycle.addObserver(
            ScrollPositionObserver(
                lifecycleOwner = lifecycleOwner,
                recyclerView = recyclerView,
                scrollPosition = scrollPosition,
                scrollListener = scrollListener
            )
        )
    }
}

private fun <ItemType : Any, BindingType : ViewDataBinding> RecyclerView.Adapter<*>.applyAdapterData(
    oldItems: MutableList<ItemType>,
    newItems: List<ItemType>?,
    pagingData: PagingData<ItemType>? = null,
    rvConfig: DataBindingRecyclerViewConfig<BindingType>
) {
    if (rvConfig.adapterType == BindingAdapterType.PAGING) {
        if (pagingData != null) {
            val currentPagingAdapter = getPagingAdapter<ItemType, BindingType>()
            if (currentPagingAdapter != null) {
                val diffItemsCallback = rvConfig.getPagingDiffUtils(currentPagingAdapter)
                diffItemsCallback?.setPageData(pagingData, currentPagingAdapter)
            }
        }
    } else if (rvConfig.diffUtilCallback != null) {
        val diffCallback = rvConfig.diffUtilCallback as? DiffCallback<Any>
        diffCallback?.setData(newItems, this)
    } else {
        applyData(this, oldItems, newItems)
    }
}


private fun <ItemType : Any, BindingType : ViewDataBinding> DataBindingRecyclerViewConfig<BindingType>.getPagingDiffUtils(
    adapter: DataBindingPagingDataAdapter<ItemType, BindingType>
): DiffItemsCallback<ItemType>? {
    return (this.diffItemsUtilCallback
        ?: adapter.getItemsDiffCallback()) as? DiffItemsCallback<ItemType>
}


private fun <ItemType : Any, BindingType : ViewDataBinding> RecyclerView.Adapter<*>.getPagingAdapter(): DataBindingPagingDataAdapter<ItemType, BindingType>? {
    return if (this is ConcatAdapter) {
        this.adapters.filterIsInstance<DataBindingPagingDataAdapter<ItemType, BindingType>>()
            .firstOrNull()
    } else this as? DataBindingPagingDataAdapter<ItemType, BindingType>
}


internal fun <ItemType : Any, BindingType : ViewDataBinding> RecyclerView.Adapter<*>.clearAdapter() {
    getPagingAdapter<ItemType, BindingType>()?.clearAdapter()
        ?: (this as? DataBindingRecyclerAdapter<ItemType, BindingType>)?.clearAdapter()
}


private fun <ItemType : Any, BindingType : ViewDataBinding> View.getOrCreateOldItems(
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>
): MutableList<ItemType> {
    var oldItems: MutableList<ItemType>? = null
    val isStandardAdapter = dataBindingRecyclerViewConfig.adapterType == BindingAdapterType.STANDARD
    if (isStandardAdapter && dataBindingRecyclerViewConfig.diffUtilCallback == null) {
        this.tag?.let {
            oldItems = it as? MutableList<ItemType>
        }

        if (oldItems == null) {
            oldItems = mutableListOf()
        }
        this.tag = oldItems
    }
    return oldItems ?: mutableListOf()
}

fun <ItemType : Any, BindingType : ViewDataBinding> DataBindingRecyclerViewConfig<BindingType>.createAdapter(
    items: List<ItemType>
): RecyclerView.Adapter<*> {
    val erbAdapter = ErbAdapter(
        items = items,
        lifecycleOwner = this.lifecycleOwner,
        layoutId = this.layoutId,
        itemId = this.itemId,
        doubleClickDelayTime = this.doubleClickDelayTime,
        consumeLongClick = this.consumeLongClick,
        isLifecyclePending = this.isLifecyclePending,
        realisation = this.realisation,
        onItemPosClickListener = this.onItemPosClickListener,
        onItemClickListener = this.onItemClickListener,
        onItemDoubleClickListener = this.onItemDoubleClickListener,
        onItemLongClickListener = this.onItemLongClickListener
    )
    return when (adapterType) {
        BindingAdapterType.LOADING -> DataBindingLoadStateAdapter(erbAdapter)
        BindingAdapterType.PAGING -> {
            // local diffs
            val localDiffItemsCallback =
                diffItemsUtilCallback ?: DiffItemsCallback<ItemType>(lifecycleOwner)
            // adapter
            DataBindingPagingDataAdapter(
                erbAdapter = erbAdapter,
                diffUtilCallback = localDiffItemsCallback as DiffItemsCallback<ItemType>
            ).run {
                if (stateFooterAdapter != null && stateHeaderAdapter != null) {
                    withLoadStateHeaderAndFooter(
                        header = stateHeaderAdapter,
                        footer = stateFooterAdapter
                    )
                } else if (stateFooterAdapter != null) {
                    withLoadStateFooter(stateFooterAdapter)
                } else if (stateHeaderAdapter != null) {
                    withLoadStateHeader(stateHeaderAdapter)
                } else {
                    this
                }
            }
        }
        else -> {
            DataBindingRecyclerAdapter(erbAdapter = erbAdapter)
        }
    }
}

@SuppressLint("NotifyDataSetChanged")
private fun <ItemType : Any> applyData(
    adapter: RecyclerView.Adapter<*>,
    oldItems: MutableList<ItemType>?,
    newItems: List<ItemType>?
) {
    var notifySize = 0
    if (oldItems !== newItems) {
        if (oldItems == null) return

        val oldSz = oldItems.size
        val firstItem = if (oldSz > 0) oldItems.first() else null
        oldItems.clear()
        newItems?.let { new ->
            val indFirst = firstItem?.let { new.indexOf(it) } ?: 0
            val newSz = new.size
            val diff = newSz - oldSz
            oldItems.addAll(new)

            when {
                diff > 0 -> {
                    // adding
                    val adSz = if (indFirst > 0) 0 else oldSz
                    adapter.notifyItemRangeInserted(adSz, diff)
                }
                diff < 0 -> {
                    val absDiff = abs(diff)
                    // deleting
                    var remSz = if (indFirst < 0) 0 else (oldSz - absDiff)
                    remSz = if (remSz < 0) 0 else remSz
                    adapter.notifyItemRangeRemoved(remSz, absDiff)
                }
                else -> Unit
            }
            notifySize = newSz
        }
    } else {
        notifySize = oldItems?.let { old ->
            old.clear()
            newItems?.let {
                old.addAll(it)
            }
            old.size
        } ?: 0
    }
    adapter.notifyItemRangeChanged(0, notifySize)
}


fun <ItemType : Any, BindingType : ViewDataBinding> RecyclerView.findPagingAdapter(): PagingDataAdapter<ItemType, BindingViewHolder>? {
    return this.adapter?.getPagingAdapter<ItemType, BindingType>()
}

fun RecyclerView.findPagingMultiAdapter(): PagingDataAdapter<IBindingModel, BindingViewHolder>? {
    return this.adapter?.getPagingAdapter<IBindingModel, ViewDataBinding>()
}

interface OnRecyclerItemClickListener {
    fun <T : Any> onItemClicked(item: T?, position: Int)
    fun <T : Any> onItemClicked(item: T?)
}

interface OnRecyclerItemDoubleClickListener {
    fun <T : Any> onItemDoubleClicked(item: T?, position: Int)
}

interface OnRecyclerItemLongClickListener {
    fun <T : Any> onItemLongClicked(item: T?, position: Int)
}