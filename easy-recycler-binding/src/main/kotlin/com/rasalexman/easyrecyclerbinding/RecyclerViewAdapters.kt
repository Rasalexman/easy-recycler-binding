@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")

package com.rasalexman.easyrecyclerbinding

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs

class CustomPageChangeCallback : ViewPager2.OnPageChangeCallback() {
    var onPageChangedCallback: (() -> Unit)? = null

    override fun onPageSelected(position: Int) {
        onPageChangedCallback?.invoke()
        super.onPageSelected(position)
    }
}

val changeCallbackMap = WeakHashMap<String, CustomPageChangeCallback>(3)

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
        viewPager.adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems)

        val callbackKey = viewPager.hashCode().toString()
        val lastCallback = changeCallbackMap.getOrPut(callbackKey) {
            CustomPageChangeCallback()
        }
        viewPager.registerOnPageChangeCallback(lastCallback)
    }

    viewPager.adapter?.applyAdapterData(
        oldItems = oldItems,
        newItems = newItems,
        dataBindingRecyclerViewConfig = dataBindingRecyclerViewConfig
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
    pageChangeCallback?.onPageChangedCallback = {
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
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?,
    scrollPosition: ScrollPosition? = null,
    pagingData: PagingData<ItemType>? = null,
    visibleThreshold: Int = RecyclerView.NO_POSITION
) {

    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    val oldItems: MutableList<ItemType> =
        recyclerView.getOrCreateOldItems(dataBindingRecyclerViewConfig)

    if (recyclerView.adapter == null) {
        val isStandardAdapter = dataBindingRecyclerViewConfig.adapterType == BindingAdapterType.STANDARD
        recyclerView.setHasFixedSize(dataBindingRecyclerViewConfig.hasFixedSize)

        var scrollListener: EndlessRecyclerOnScrollListener? = null
        if (recyclerView.layoutManager == null) {
            val mLayoutManager: RecyclerView.LayoutManager =
                dataBindingRecyclerViewConfig.layoutManager
                    ?: LinearLayoutManager(
                        recyclerView.context,
                        dataBindingRecyclerViewConfig.orientation,
                        dataBindingRecyclerViewConfig.isReverseLayout
                    )
            recyclerView.layoutManager = mLayoutManager

            if (isStandardAdapter) {
                // first clear all listeners
                recyclerView.clearOnScrollListeners()
                // custom listener
                dataBindingRecyclerViewConfig.onScrollListener?.let { onLoadMoreHandler ->
                    scrollListener = object :
                        EndlessRecyclerOnScrollListener(mLayoutManager, visibleThreshold) {
                        override fun onLoadMore(currentPage: Int) {
                            onLoadMoreHandler(currentPage)
                        }
                    }.apply(recyclerView::addOnScrollListener)
                } ?:
                // or check another scroll listener scroll listener
                dataBindingRecyclerViewConfig.recyclerOnScrollListener?.let {
                    recyclerView.addOnScrollListener(it)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.touchscreenBlocksFocus = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recyclerView.defaultFocusHighlightEnabled = false
        }

        val adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems)
        // create adapter
        recyclerView.swapAdapter(adapter, false).also {
            // invoke onAdapterAdded callback
            dataBindingRecyclerViewConfig.onAdapterAdded?.invoke(adapter)
        }

        // create scroll position observer and scroll listener recalculation
        val lifecycleOwner = try {
            dataBindingRecyclerViewConfig.lifecycleOwner
                ?: recyclerView.findFragment<Fragment>().viewLifecycleOwner
        } catch (e: Exception) {
            //println("[ERROR]: error with recyclerView.findFragment = $e")
            recyclerView.context.run {
                findPrimaryFragment()?.viewLifecycleOwner ?: getOwner<LifecycleOwner>()
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

        dataBindingRecyclerViewConfig.itemAnimator?.let {
            recyclerView.itemAnimator = it
        }

        dataBindingRecyclerViewConfig.itemDecorator?.let { decorationList ->
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
        dataBindingRecyclerViewConfig = dataBindingRecyclerViewConfig
    )
}

fun <ItemType : Any, BindingType : ViewDataBinding> RecyclerView.Adapter<*>.applyAdapterData(
    oldItems: MutableList<ItemType>,
    newItems: List<ItemType>?,
    pagingData: PagingData<ItemType>? = null,
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>
) {

    if (dataBindingRecyclerViewConfig.adapterType == BindingAdapterType.PAGING) {
        if (pagingData != null) {
            val currentPagingAdapter = getPagingAdapter<ItemType, BindingType>()
            if (currentPagingAdapter != null) {
                val diffItemsCallback =
                    dataBindingRecyclerViewConfig.getPagingDiffUtils(currentPagingAdapter)
                diffItemsCallback?.setPageData(pagingData, currentPagingAdapter)
            }
        }
    } else if (dataBindingRecyclerViewConfig.diffUtilCallback != null) {
        val diffCallback = dataBindingRecyclerViewConfig.diffUtilCallback as? DiffCallback<Any>
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
            .getOrNull(0)
    } else this as? DataBindingPagingDataAdapter<ItemType, BindingType>
}


private fun <ItemType : Any, BindingType : ViewDataBinding> RecyclerView.Adapter<*>.clearAdapter() {
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

private fun <ItemType : Any, BindingType : ViewDataBinding> DataBindingRecyclerViewConfig<BindingType>.createAdapter(
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

internal class ScrollPositionObserver(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    scrollPosition: ScrollPosition?,
    scrollListener: EndlessRecyclerOnScrollListener?
) : LifecycleObserver {

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

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifecycleOwnerStopped() {
        saveScrollPosition()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifecycleOwnerResumed() {
        adapterDataObserver?.changeScrollPosition(1)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleOwnerDestroyed() {
        clearWhenViewDestroy()
    }

    private fun saveScrollPosition() {
        scrollPositionWeakRef.get()?.let { scrollPosition ->
            recyclerWeakRef.get()?.let { currentRV ->
                currentRV.stopScroll()
                val linearLayoutManager = (currentRV.layoutManager as? LinearLayoutManager?)
                val isHorizontal = linearLayoutManager?.orientation == LinearLayout.HORIZONTAL

                val position = if(isHorizontal) {
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

internal class ItemsDataObserver(
    recyclerView: RecyclerView,
    scrollPosition: ScrollPosition?,
    scrollListener: EndlessRecyclerOnScrollListener?
) : RecyclerView.AdapterDataObserver() {
    private val scrollPositionWeakRef = WeakReference(scrollPosition)
    private val recyclerWeakRef = WeakReference(recyclerView)
    private val scrollListenerWeakRef = WeakReference(scrollListener)

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        resetScrollListenerTotalCount()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (positionStart == 0) {
            resetScrollListenerTotalCount()
            // will be removed in future releases
            //changeScrollPosition(itemCount)
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
                        val linearLayoutManager = (recyclerView.layoutManager as? LinearLayoutManager?)
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

enum class BindingAdapterType {
    STANDARD, PAGING, LOADING
}

data class DataBindingRecyclerViewConfig<BindingType : ViewDataBinding>(
    val layoutId: Int,
    val itemId: Int,
    val adapterType: BindingAdapterType = BindingAdapterType.STANDARD,
    val orientation: Int = RecyclerView.VERTICAL,
    val consumeLongClick: Boolean = true,
    val doubleClickDelayTime: Long = 150L,
    val lifecycleOwner: LifecycleOwner? = null,
    val realisation: DataBindingAdapter<BindingType>? = null,
    val onItemClickListener: OnRecyclerItemClickListener? = null,
    val onItemLongClickListener: OnRecyclerItemLongClickListener? = null,
    val onItemDoubleClickListener: OnRecyclerItemDoubleClickListener? = null,
    val onScrollListener: ((Int) -> Unit)? = null,
    val layoutManager: RecyclerView.LayoutManager? = null,
    val isReverseLayout: Boolean = false,
    val recyclerOnScrollListener: RecyclerView.OnScrollListener? = null,
    val itemAnimator: RecyclerView.ItemAnimator? = null,
    val itemDecorator: List<RecyclerView.ItemDecoration>? = null,
    val diffUtilCallback: DiffCallback<*>?,
    val diffItemsUtilCallback: DiffItemsCallback<*>?,
    val hasFixedSize: Boolean = true,
    val isLifecyclePending: Boolean = true,
    val stateFooterAdapter: DataBindingLoadStateAdapter<ILoadStateModel, ViewDataBinding>? = null,
    val stateHeaderAdapter: DataBindingLoadStateAdapter<ILoadStateModel, ViewDataBinding>? = null,
    val onAdapterAdded: ((RecyclerView.Adapter<*>) -> Unit)? = null,
) {

    class DataBindingRecyclerViewConfigBuilder<I : Any, BT : ViewDataBinding> {
        var layoutId: Int? = null
        var itemId: Int? = null
        var doubleClickDelayTime: Long = 150L
        var adapterType: BindingAdapterType = BindingAdapterType.STANDARD
        var consumeLongClick: Boolean = true
        var lifecycleOwner: LifecycleOwner? = null
        var onItemCreate: ((BT) -> Unit)? = null
        var onItemUnbind: ((BT) -> Unit)? = null
        var orientation: Int = RecyclerView.VERTICAL
        var onItemBind: ((BT, Int) -> Unit)? = null
        var onLoadMore: ((Int) -> Unit)? = null
        var onItemClick: ((I, Int) -> Unit)? = null
        var onModelBind: ((I, Int) -> Unit)? = null
        var onItemDoubleClicked: ((I, Int) -> Unit)? = null
        var onItemLongClickListener: ((I, Int) -> Unit)? = null
        var layoutManager: RecyclerView.LayoutManager? = null
        var onScrollListener: RecyclerView.OnScrollListener? = null
        var itemAnimator: RecyclerView.ItemAnimator? = null
        var itemDecorator: List<RecyclerView.ItemDecoration>? = null
        var diffUtilCallback: DiffCallback<*>? = null
        var diffItemUtilCallback: DiffItemsCallback<I>? = null
        var hasFixedSize: Boolean = true
        var isLifecyclePending: Boolean = true
        var isReverseLayout: Boolean = false
        var onAdapterAdded: ((RecyclerView.Adapter<*>) -> Unit)? = null

        var stateFooterAdapter: DataBindingLoadStateAdapter<ILoadStateModel, ViewDataBinding>? =
            null
        var stateHeaderAdapter: DataBindingLoadStateAdapter<ILoadStateModel, ViewDataBinding>? =
            null

        fun build(): DataBindingRecyclerViewConfig<BT> {

            return DataBindingRecyclerViewConfig(
                layoutId = layoutId ?: -1,
                itemId = itemId
                    ?: throw NullPointerException("DataBindingRecyclerViewConfig::itemId must not be null"),
                adapterType = adapterType,
                lifecycleOwner = lifecycleOwner,
                orientation = orientation,
                doubleClickDelayTime = doubleClickDelayTime,
                consumeLongClick = consumeLongClick,
                layoutManager = layoutManager,
                onScrollListener = onLoadMore,
                recyclerOnScrollListener = onScrollListener,
                itemAnimator = itemAnimator,
                itemDecorator = itemDecorator,
                diffUtilCallback = diffUtilCallback,
                diffItemsUtilCallback = diffItemUtilCallback,
                hasFixedSize = hasFixedSize,
                isLifecyclePending = isLifecyclePending,
                isReverseLayout = isReverseLayout,

                stateFooterAdapter = stateFooterAdapter,
                stateHeaderAdapter = stateHeaderAdapter,

                realisation = object : DataBindingAdapter<BT> {

                    override fun onCreate(binding: BT) {
                        onItemCreate?.invoke(binding)
                    }

                    override fun onBind(binding: BT, position: Int) {
                        onItemBind?.invoke(binding, position)
                    }

                    override fun onUnbind(binding: BT) {
                        onItemUnbind?.invoke(binding)
                    }

                    override fun <T : Any> onBindItem(item: T?, position: Int) {
                        onModelBind?.let { modelHandler ->
                            val boundItem = (item as? I)
                            boundItem?.let {
                                modelHandler.invoke(it, position)
                            }
                        }
                    }
                },
                onItemClickListener = object : OnRecyclerItemClickListener {
                    override fun <T : Any> onItemClicked(item: T?, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            onItemClick?.invoke(it, position)
                        }
                    }
                },
                onItemDoubleClickListener = object : OnRecyclerItemDoubleClickListener {
                    override fun <T : Any> onItemDoubleClicked(item: T?, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            onItemDoubleClicked?.invoke(it, position)
                        }
                    }
                },
                onItemLongClickListener = object : OnRecyclerItemLongClickListener {
                    override fun <T : Any> onItemLongClicked(item: T?, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            onItemLongClickListener?.invoke(it, position)
                        }
                    }
                },
                onAdapterAdded = onAdapterAdded
            )
        }
    }
}


class DataBindingLoadStateAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val erbAdapter: ErbAdapter<ItemType, BindingType>
) : LoadStateAdapter<BindingViewHolder>() {

    init {
        erbAdapter.onGetItemsCountHandler = this::getItemCount
    }

    override fun onBindViewHolder(holder: BindingViewHolder, loadState: LoadState) {
        erbAdapter.onBindViewHolder(holder, 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BindingViewHolder {
        return erbAdapter.onCreateViewHolder(parent, erbAdapter.getItemViewType(0))
    }
}

internal class DataBindingPagingDataAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val erbAdapter: ErbAdapter<ItemType, BindingType>,
    diffUtilCallback: DiffItemsCallback<ItemType>
) : PagingDataAdapter<ItemType, BindingViewHolder>(diffUtilCallback),
    IErbAdapter<ItemType, BindingType> by erbAdapter {

    private val diffItemsUtilsWeakRef = WeakReference(diffUtilCallback)

    fun getItemsDiffCallback(): DiffItemsCallback<ItemType>? {
        return diffItemsUtilsWeakRef.get()
    }

    init {
        erbAdapter.onGetItemHandler = ::getItem
        erbAdapter.onGetItemsCountHandler = ::getItemCount
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return erbAdapter.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return erbAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        erbAdapter.onBindViewHolder(holder, position)
    }

    override fun onViewRecycled(holder: BindingViewHolder) {
        erbAdapter.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun clearAdapter() {
        diffItemsUtilsWeakRef.clear()
        erbAdapter.clearAdapter()
    }
}

internal class DataBindingRecyclerAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val erbAdapter: ErbAdapter<ItemType, BindingType>
) : RecyclerView.Adapter<BindingViewHolder>(),
    IErbAdapter<ItemType, BindingType> by erbAdapter {

    override fun getItemCount(): Int {
        return erbAdapter.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return erbAdapter.getItemViewType(position)
    }

    override fun getAdapterItems(): List<ItemType> {
        return erbAdapter.getAdapterItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return erbAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        erbAdapter.onBindViewHolder(holder, position)
    }

    override fun onViewRecycled(holder: BindingViewHolder) {
        erbAdapter.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun clearAdapter() {
        erbAdapter.clearAdapter()
    }
}

abstract class ViewClickersListener(
    private val hasDoubleClickListener: Boolean,
    private val doubleClickDelayTime: Long,
    private val consumeLongClick: Boolean = true
) : View.OnClickListener, View.OnLongClickListener {

    private var lastClickCount: Int = 0

    override fun onClick(v: View) {
        if (hasDoubleClickListener) {
            processDoubleClick()
        } else {
            onSingleClicked()
        }
    }

    private fun processDoubleClick() {
        if (lastClickCount == 0) {
            lastClickCount++
            Handler(Looper.getMainLooper()).postDelayed({
                if (lastClickCount == 1) {
                    onSingleClicked()
                } else {
                    onDoubleClicked()
                }
                lastClickCount = 0
            }, doubleClickDelayTime)
        } else {
            lastClickCount++
        }
    }

    // true if the callback consumed the long click, false otherwise.
    override fun onLongClick(view: View): Boolean {
        onLongClicked()
        return consumeLongClick
    }

    abstract fun onLongClicked()

    abstract fun onSingleClicked()

    abstract fun onDoubleClicked()
}

fun <I : Any, BT : ViewDataBinding> recyclerConfig(block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit): DataBindingRecyclerViewConfig<BT> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>().apply(block)
        .build().also {
            if (it.layoutId == -1) {
                throw IllegalStateException("layoutId is not set for viewHolders adapter")
            }
        }
}

fun <I : ILoadStateModel> createStateAdapter(
    item: I,
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, ViewDataBinding>.() -> Unit
): DataBindingLoadStateAdapter<I, ViewDataBinding> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, ViewDataBinding>()
        .apply(block)
        .also {
            it.adapterType = BindingAdapterType.LOADING
        }.build().createAdapter(
            items = listOf(item)
        ) as DataBindingLoadStateAdapter<I, ViewDataBinding>
}

fun recyclerMultiConfig(block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>.() -> Unit): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>()
        .apply(block).build()
}

interface OnRecyclerItemClickListener {
    fun <T : Any> onItemClicked(item: T?, position: Int)
}

interface OnRecyclerItemDoubleClickListener {
    fun <T : Any> onItemDoubleClicked(item: T?, position: Int)
}

interface OnRecyclerItemLongClickListener {
    fun <T : Any> onItemLongClicked(item: T?, position: Int)
}