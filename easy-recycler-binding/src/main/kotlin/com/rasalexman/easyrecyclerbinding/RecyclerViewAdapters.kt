package com.rasalexman.easyrecyclerbinding

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.paging.PagingDataAdapter
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

@BindingAdapter(value = ["items", "rv_config", "position", "visibleThreshold"], requireAll = false)
fun <ItemType : Any, BindingType : ViewDataBinding> setupRecyclerView(
    recyclerView: RecyclerView,
    newItems: List<ItemType>?,
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?,
    scrollPosition: ScrollPosition? = null,
    visibleThreshold: Int = RecyclerView.NO_POSITION
) {

    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    val oldItems: MutableList<ItemType> =
        recyclerView.getOrCreateOldItems(dataBindingRecyclerViewConfig)

    if (recyclerView.adapter == null) {
        recyclerView.setHasFixedSize(dataBindingRecyclerViewConfig.hasFixedSize)

        if (scrollPosition != null) {
            try {
                val lifecycleOwner =
                    dataBindingRecyclerViewConfig.lifecycleOwner ?: recyclerView.findFragment()
                lifecycleOwner.lifecycle.addObserver(
                    ScrollPositionObserver(lifecycleOwner, recyclerView, scrollPosition)
                )
            } catch (e: Exception) {
                println("[ERROR]: error with recyclerView.findFragment = $e")
            }
        }

        var scrollListener: RecyclerView.OnScrollListener? = null
        if (recyclerView.layoutManager == null) {
            val mLayoutManager: RecyclerView.LayoutManager =
                dataBindingRecyclerViewConfig.layoutManager
                    ?: LinearLayoutManager(
                        recyclerView.context,
                        dataBindingRecyclerViewConfig.orientation,
                        false
                    )
            recyclerView.layoutManager = mLayoutManager
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.touchscreenBlocksFocus = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recyclerView.defaultFocusHighlightEnabled = false
        }

        recyclerView.adapter =
            dataBindingRecyclerViewConfig.createAdapter(oldItems).also { adapter ->
                scrollListener?.let { listener ->
                    adapter.registerAdapterDataObserver(object :
                        RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                            (listener as? EndlessRecyclerOnScrollListener)?.resetTotalCount(adapter.itemCount)
                        }

                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            if (positionStart == 0) {
                                //println("-----> onItemRangeInserted = $positionStart")
                                (listener as? EndlessRecyclerOnScrollListener)?.resetTotalCount(
                                    adapter.itemCount
                                )
                            }
                        }
                    })
                }
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

    val oldItemsCount = recyclerView.adapter?.itemCount ?: 0
    recyclerView.adapter?.applyAdapterData(
        oldItems = oldItems,
        newItems = newItems,
        dataBindingRecyclerViewConfig = dataBindingRecyclerViewConfig
    )
    scrollPosition?.let {
        val newItemsCount = recyclerView.adapter?.itemCount ?: 0
        if (oldItemsCount == 0 && newItemsCount > 0) {
            recyclerView.stopScroll()
            (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                it.index,
                it.top
            )
        }
    }
}

internal class ScrollPositionObserver(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    scrollPosition: ScrollPosition
) : LifecycleObserver {

    private val ownerWeakRef = WeakReference(lifecycleOwner)
    private val recyclerWeakRef = WeakReference<RecyclerView>(recyclerView)
    private val scrollWeakRef = WeakReference(scrollPosition)

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onSaveScroll() {
        scrollWeakRef.get()?.let { scrollPosition ->
            recyclerWeakRef.get()?.let { currentRV ->
                currentRV.stopScroll()
                scrollPosition.apply {
                    index = (currentRV.layoutManager as? LinearLayoutManager?)?.findFirstVisibleItemPosition() ?: 0
                    top = currentRV.getChildAt(0)?.let { it.top - currentRV.paddingTop } ?: 0
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        ownerWeakRef.get()?.lifecycle?.removeObserver(this)
        ownerWeakRef.clear()
        scrollWeakRef.clear()
        recyclerWeakRef.clear()
    }
}

private fun <ItemType : Any, BindingType : ViewDataBinding> View.getOrCreateOldItems(
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>
): MutableList<ItemType> {
    var oldItems: MutableList<ItemType>? = null
    if (dataBindingRecyclerViewConfig.diffUtilCallback == null) {
        this.tag?.let {
            @Suppress("UNCHECKED_CAST")
            oldItems = it as? MutableList<ItemType>
        }

        if (oldItems == null) {
            oldItems = mutableListOf()
            this.tag = oldItems
        }
    }
    return oldItems ?: mutableListOf()
}

fun<ItemType : Any, BindingType : ViewDataBinding> RecyclerView.Adapter<RecyclerView.ViewHolder>.applyAdapterData(
    oldItems: MutableList<ItemType>,
    newItems: List<ItemType>?,
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>
) {
    val diffCallback = dataBindingRecyclerViewConfig.diffUtilCallback as? DiffCallback<Any>
    diffCallback?.setData(newItems, this)
        ?: applyData(this, oldItems, newItems)
}

@Suppress("UNCHECKED_CAST")
private fun <ItemType : Any, BindingType : ViewDataBinding> DataBindingRecyclerViewConfig<BindingType>.createAdapter(
    items: List<ItemType>
): RecyclerView.Adapter<BindingViewHolder> {
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
    return if (this.adapterType == BindingAdapterType.STANDARD)
        DataBindingRecyclerAdapter(erbAdapter = erbAdapter)
    else {
        val localDiffItemsCallback =
            (diffItemsUtilCallback as? DiffItemsCallback<ItemType>) ?: DiffItemsCallback<ItemType>(
                this.lifecycleOwner
            )
        DataBindingPagingDataAdapter<ItemType, BindingType>(
            erbAdapter = erbAdapter,
            diffUtilCallback = localDiffItemsCallback
        )
    }
}

private fun <ItemType : Any> applyData(
    adapter: RecyclerView.Adapter<*>,
    oldItems: MutableList<ItemType>?,
    newItems: List<ItemType>?
) {
    if (oldItems !== newItems) {
        oldItems?.let { old ->
            val oldSz = old.size
            val firstItem = if (oldSz > 0) old.first() else null
            old.clear()
            newItems?.let { new ->
                val indFirst = firstItem?.let { new.indexOf(it) } ?: 0
                val newSz = new.size
                val diff = newSz - oldSz
                old.addAll(new)
                when {
                    diff > 0 -> {
                        // adding
                        val adSz = if (indFirst > 0) 0 else oldSz
                        adapter.notifyItemRangeInserted(adSz, diff)
                    }
                    diff < 0 -> {
                        val remDiffSz = abs(diff)
                        // deleting
                        val remSz = if (indFirst < 0) 0 else (oldSz - remDiffSz - 1)
                        adapter.notifyItemRangeRemoved(remSz, remDiffSz)
                    }
                    else -> {
                        adapter.notifyDataSetChanged()
                    }
                }
            } ?: adapter.notifyDataSetChanged()
        }
    } else {
        oldItems?.let { old ->
            newItems?.let {
                old.addAll(it)
            }
            adapter.notifyDataSetChanged()
        }
    }
}

enum class BindingAdapterType {
    STANDARD, PAGING
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
    val recyclerOnScrollListener: RecyclerView.OnScrollListener? = null,
    val itemAnimator: RecyclerView.ItemAnimator? = null,
    val itemDecorator: List<RecyclerView.ItemDecoration>? = null,
    val diffUtilCallback: DiffCallback<*>?,
    val diffItemsUtilCallback: DiffItemsCallback<*>?,
    val hasFixedSize: Boolean = true,
    val isLifecyclePending: Boolean = false,
    val scrollPosition: ScrollPosition? = null
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
        var onItemDoubleClicked: ((I, Int) -> Unit)? = null
        var onItemLongClickListener: ((I, Int) -> Unit)? = null
        var layoutManager: RecyclerView.LayoutManager? = null
        var onScrollListener: RecyclerView.OnScrollListener? = null
        var itemAnimator: RecyclerView.ItemAnimator? = null
        var itemDecorator: List<RecyclerView.ItemDecoration>? = null
        var diffUtilCallback: DiffCallback<*>? = null
        var diffItemUtilCallback: DiffItemsCallback<I>? = null
        var hasFixedSize: Boolean = true
        var isLifecyclePending: Boolean = false
        var scrollPosition: ScrollPosition? = null

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
                diffItemsUtilCallback = diffItemUtilCallback as? DiffItemsCallback<I>,
                hasFixedSize = hasFixedSize,
                isLifecyclePending = isLifecyclePending,
                scrollPosition = scrollPosition,
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
                },
                onItemClickListener = object : OnRecyclerItemClickListener {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : Any> onItemClicked(item: T, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            onItemClick?.invoke(it, position)
                        }
                    }
                },
                onItemDoubleClickListener = object : OnRecyclerItemDoubleClickListener {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : Any> onItemDoubleClicked(item: T, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            onItemDoubleClicked?.invoke(it, position)
                        }
                    }
                },
                onItemLongClickListener = object : OnRecyclerItemLongClickListener {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : Any> onItemLongClicked(item: T, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            onItemLongClickListener?.invoke(it, position)
                        }
                    }
                }
            )
        }
    }
}

internal class DataBindingPagingDataAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val erbAdapter: ErbAdapter<ItemType, BindingType>,
    val diffUtilCallback: DiffItemsCallback<ItemType>
) : PagingDataAdapter<ItemType, BindingViewHolder>(diffUtilCallback),
    IErbAdapter<ItemType, BindingType> by erbAdapter {

    override fun getItemCount(): Int {
        return erbAdapter.getItemCount()
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
}

interface DataBindingAdapter<BindingType : ViewDataBinding> {
    fun onCreate(binding: BindingType)
    fun onBind(binding: BindingType, position: Int)
    fun onUnbind(binding: BindingType)
}

interface ItemsBinderAdapter<ItemType : Any> {
    fun setAdapterItems(items: List<ItemType>)
    fun getAdapterItems(): List<ItemType>
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

inline fun <I : Any, BT : ViewDataBinding> recyclerConfig(block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit): DataBindingRecyclerViewConfig<BT> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>().apply(block)
        .build().also {
            if (it.layoutId == -1) {
                throw IllegalStateException("layoutId is not set for viewHolders adapter")
            }
        }
}

inline fun recyclerMultiConfig(block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>.() -> Unit): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>()
        .apply(block).build()
}

interface OnRecyclerItemClickListener {
    fun <T : Any> onItemClicked(item: T, position: Int)
}

interface OnRecyclerItemDoubleClickListener {
    fun <T : Any> onItemDoubleClicked(item: T, position: Int)
}

interface OnRecyclerItemLongClickListener {
    fun <T : Any> onItemLongClicked(item: T, position: Int)
}