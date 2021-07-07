package com.rasalexman.easyrecyclerbinding

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.CoroutineContext

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

    var oldItems: MutableList<ItemType>? = null
    val diffCallback = dataBindingRecyclerViewConfig.diffUtilCallback as? DiffCallback<ItemType>

    if (diffCallback == null) {
        viewPager.tag?.let {
            @Suppress("UNCHECKED_CAST")
            oldItems = it as? MutableList<ItemType>
        }

        if (oldItems == null) {
            oldItems = mutableListOf()
            viewPager.tag = oldItems
        }
    }

    if (viewPager.adapter == null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewPager.defaultFocusHighlightEnabled = false
        }
        viewPager.adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems.orEmpty())

        val callbackKey = viewPager.hashCode().toString()
        val lastCallback = changeCallbackMap.getOrPut(callbackKey) {
            CustomPageChangeCallback()
        }
        viewPager.registerOnPageChangeCallback(lastCallback)
    }

    val adapter = viewPager.adapter!!
    diffCallback?.setData(newItems.orEmpty(), adapter)
        ?: applyData(adapter, oldItems, newItems)
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

@BindingAdapter(value = ["items", "rv_config", "visibleThreshold"], requireAll = false)
fun <ItemType : Any, BindingType : ViewDataBinding> setupRecyclerView(
    recyclerView: RecyclerView,
    newItems: List<ItemType>?,
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?,
    visibleThreshold: Int = 5
) {

    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    var oldItems: MutableList<ItemType>? = null

    val diffCallback = dataBindingRecyclerViewConfig.diffUtilCallback as? DiffCallback<ItemType>

    if (diffCallback == null) {
        recyclerView.tag?.let {
            @Suppress("UNCHECKED_CAST")
            oldItems = it as? MutableList<ItemType>
        }

        if (oldItems == null) {
            oldItems = mutableListOf()
            recyclerView.tag = oldItems
        }
    }

    if (recyclerView.adapter == null) {
        recyclerView.setHasFixedSize(dataBindingRecyclerViewConfig.hasFixedSize)

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
                recyclerView.addOnScrollListener(object :
                    EndlessRecyclerOnScrollListener(mLayoutManager, visibleThreshold) {
                    override fun onLoadMore(currentPage: Int) {
                        onLoadMoreHandler(currentPage)
                    }
                })
            }
            // scroll listener
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

        recyclerView.adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems.orEmpty())

        dataBindingRecyclerViewConfig.itemAnimator?.let {
            if (newItems != null) {
                recyclerView.itemAnimator = it
            }
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

    recyclerView.stopScroll()
    val adapter = recyclerView.adapter!!
    diffCallback?.setData(newItems.orEmpty(), adapter)
        ?: applyData(adapter, oldItems, newItems)

}

@Suppress("UNCHECKED_CAST")
private fun <ItemType : Any, BindingType : ViewDataBinding> DataBindingRecyclerViewConfig<BindingType>.createAdapter(
    items: List<ItemType>
): RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder> {
    return DataBindingRecyclerAdapter(
        items = items,
        lifecycleOwner = this.lifecycleOwner,
        layoutId = this.layoutId,
        itemId = this.itemId,
        doubleClickDelayTime = this.doubleClickDelayTime,
        consumeLongClick = this.consumeLongClick,
        realisation = this.realisation,
        onItemClickListener = this.onItemClickListener,
        onItemDoubleClickListener = this.onItemDoubleClickListener,
        onItemLongClickListener = this.onItemLongClickListener
    )
}

private fun <ItemType : Any> applyData(
    adapter: RecyclerView.Adapter<*>,
    oldItems: MutableList<ItemType>?,
    newItems: List<ItemType>?
) {
    if (oldItems !== newItems) {
        oldItems?.let { old ->
            old.clear()
            newItems?.let {
                old.addAll(newItems)
            }
        }
    } else {
        oldItems?.let { old ->
            newItems?.let {
                old.addAll(newItems)
            }
        }
    }
    adapter.notifyDataSetChanged()
}

open class DiffCallback<ItemType : Any> : DiffUtil.Callback(), CoroutineScope {
    private var oldData: List<ItemType>? = null
    private var newData: List<ItemType>? = null

    private var lastDiffUtil: DiffUtil.DiffResult? = null
    private var lastJob: Job? = null

    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + supervisorJob

    open fun setData(fresh: List<ItemType>, adapter: RecyclerView.Adapter<*>) {
        clearLastJob()

        val itemsAdapter = adapter as ItemsBinderAdapter<ItemType>
        oldData = itemsAdapter.getAdapterItems()
        newData = fresh
        lastJob = launch {
            //val startTime = System.currentTimeMillis()
            //println("-----> start processing ")
            lastDiffUtil = processCalculationAsync()
            lastDiffUtil?.let {
                itemsAdapter.setAdapterItems(fresh)
                it.dispatchUpdatesTo(adapter)
                //val finishTime = System.currentTimeMillis() - startTime
                //println("-----> finish processing in ${finishTime}ms")
            }
        }
    }

    protected open fun clearLastJob() {
        lastDiffUtil = null
        lastJob?.cancel()
        lastJob = null
        supervisorJob.cancelChildren()
    }

    protected open suspend fun processCalculationAsync() = withContext(Dispatchers.Default) {
        DiffUtil.calculateDiff(this@DiffCallback)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areContentsTheSame(
            oldData?.getOrNull(oldItemPosition),
            newData?.getOrNull(newItemPosition)
        )
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(
            oldData?.getOrNull(oldItemPosition),
            newData?.getOrNull(newItemPosition)
        )
    }

    open fun areContentsTheSame(oldItem: ItemType?, newItem: ItemType?): Boolean {
        return if (oldItem != null && newItem != null) {
            oldItem == newItem
        } else if (oldItem == null && newItem == null) {
            return true
        } else {
            throw AssertionError()
        }
    }

    open fun areItemsTheSame(oldItem: ItemType?, newItem: ItemType?): Boolean {
        return if (oldItem != null && newItem != null) {
            oldItem == newItem
        } else {
            oldItem == null && newItem == null
        }
    }

    override fun getOldListSize(): Int {
        return oldData?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newData?.size ?: 0
    }

    open fun clear() {
        clearLastJob()
        oldData = null
        newData = null
    }
}


data class DataBindingRecyclerViewConfig<BindingType : ViewDataBinding>(
    val layoutId: Int,
    val itemId: Int,
    val orientation: Int = RecyclerView.VERTICAL,
    val consumeLongClick: Boolean = true,
    val doubleClickDelayTime: Long = 150L,
    val clickDebounceInterval: Long = 400L,
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
    val hasFixedSize: Boolean = true
) {

    class DataBindingRecyclerViewConfigBuilder<I : Any, BT : ViewDataBinding> {
        var layoutId: Int? = null
        var itemId: Int? = null
        var doubleClickDelayTime: Long = 150L
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
        var diffUtilCallback: DiffCallback<I>? = null
        var hasFixedSize: Boolean = true

        fun build(): DataBindingRecyclerViewConfig<BT> {
            return DataBindingRecyclerViewConfig(
                layoutId = layoutId ?: -1,
                itemId = itemId
                    ?: throw NullPointerException("DataBindingRecyclerViewConfig::itemId must not be null"),
                lifecycleOwner = lifecycleOwner,
                orientation = orientation,
                doubleClickDelayTime = doubleClickDelayTime,
                consumeLongClick = consumeLongClick,
                layoutManager = layoutManager,
                recyclerOnScrollListener = onScrollListener,
                itemAnimator = itemAnimator,
                itemDecorator = itemDecorator,
                diffUtilCallback = diffUtilCallback,
                hasFixedSize = hasFixedSize,
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
                },
                onScrollListener = onLoadMore
            )
        }
    }
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

internal class DataBindingRecyclerAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private var items: List<ItemType>,
    private val layoutId: Int,
    private val itemId: Int,
    private val doubleClickDelayTime: Long = 150L,
    private val consumeLongClick: Boolean = true,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val realisation: DataBindingAdapter<BindingType>? = null,
    private val onItemClickListener: OnRecyclerItemClickListener? = null,
    private val onItemLongClickListener: OnRecyclerItemLongClickListener? = null,
    private val onItemDoubleClickListener: OnRecyclerItemDoubleClickListener? = null
) : RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder>(),
    DataBindingAdapter<BindingType>, ItemsBinderAdapter<ItemType> {

    private var parentFragmentLifecycleOwner: WeakReference<LifecycleOwner>? = null

    override fun getItemCount(): Int = items.size

    override fun setAdapterItems(items: List<ItemType>) {
        this.items = items
    }

    override fun getAdapterItems(): List<ItemType> {
        return this.items
    }

    override fun getItemViewType(position: Int): Int {
        val viewType = layoutId.takeIf { it != -1 }
            ?: (items.getOrNull(position) as? IBindingModel)?.layoutResId
        return viewType ?: throw IllegalStateException(
            "layoutId is not set for viewHolder with item data ${
                items.getOrNull(
                    position
                )
            } "
        )
    }

    private fun getLifecycleOwner(view: View): LifecycleOwner {
        return parentFragmentLifecycleOwner?.get()
            ?: view.context.getOwner<LifecycleOwner>().apply {
                parentFragmentLifecycleOwner = WeakReference(this)
            }
    }

    private fun setupLifecycleOwner(binding: BindingType) {
        if (lifecycleOwner == null) {
            if (binding.lifecycleOwner == null) {
                val parentOwner = try {
                    getLifecycleOwner(binding.root)
                } catch (e: Exception) {
                    null
                }
                binding.lifecycleOwner = parentOwner
            }
        } else {
            binding.lifecycleOwner = lifecycleOwner
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = DataBindingUtil.inflate<BindingType>(
            LayoutInflater.from(parent.context),
            viewType,
            parent,
            false
        )

        val holder = BindingViewHolder(binding)
        setupLifecycleOwner(binding)
        onCreate(binding)

        val clickListener = object : ViewClickersListener(
            hasDoubleClickListener = onItemDoubleClickListener != null,
            doubleClickDelayTime = doubleClickDelayTime,
            consumeLongClick = consumeLongClick
        ) {

            override fun onSingleClicked() {
                val position = holder.absoluteAdapterPosition
                onItemClickListener?.onItemClicked(items[position], position)
            }

            override fun onDoubleClicked() {
                val position = holder.absoluteAdapterPosition
                onItemDoubleClickListener?.onItemDoubleClicked(items[position], position)
            }

            override fun onLongClicked() {
                val position = holder.absoluteAdapterPosition
                onItemLongClickListener?.onItemLongClicked(items[position], position)
            }
        }

        holder.binding.executePendingBindings()
        binding.root.apply {
            if (onItemClickListener != null || onItemDoubleClickListener != null) {
                setOnClickListener(clickListener)
            }
            onItemLongClickListener?.let {
                setOnLongClickListener(clickListener)
            }
        }
        return holder
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val absolutePosition = holder.absoluteAdapterPosition
        if (itemId != -1) {
            holder.binding.setVariable(itemId, items[absolutePosition])
        }
        onBind(holder.binding as BindingType, absolutePosition)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: BindingViewHolder) {
        onUnbind(holder.binding as BindingType)
        holder.binding.unbind()
        super.onViewRecycled(holder)
    }

    override fun onCreate(binding: BindingType) {
        realisation?.onCreate(binding)
    }

    override fun onBind(binding: BindingType, position: Int) {
        realisation?.onBind(binding, position)
    }

    override fun onUnbind(binding: BindingType) {
        realisation?.onUnbind(binding)
    }

    class BindingViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
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

interface OnRecyclerItemClickListener {
    fun <T : Any> onItemClicked(item: T, position: Int)
}

interface OnRecyclerItemDoubleClickListener {
    fun <T : Any> onItemDoubleClicked(item: T, position: Int)
}

interface OnRecyclerItemLongClickListener {
    fun <T : Any> onItemLongClicked(item: T, position: Int)
}