package com.rasalexman.easyrecyclerbinding

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.databinding.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class CustomPageChangeCallback : ViewPager2.OnPageChangeCallback() {
    var onPageChangedCallback: (() -> Unit)? = null

    override fun onPageSelected(position: Int) {
        onPageChangedCallback?.invoke()
        super.onPageSelected(position)
    }
}

private var customOnPageChangeCallback: CustomPageChangeCallback = CustomPageChangeCallback()

@BindingAdapter(value = ["items", "vp_config"], requireAll = false)
fun <ItemType : Any, BindingType : ViewDataBinding> setupViewPager2(
    viewPager: ViewPager2,
    newItems: List<ItemType>?,
    dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?
) {
    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    var oldItems: MutableList<ItemType>? = null

    viewPager.tag?.let {
        @Suppress("UNCHECKED_CAST")
        oldItems = it as? MutableList<ItemType>
    }

    if (oldItems == null) {
        oldItems = mutableListOf()
        viewPager.tag = oldItems
    }

    if (viewPager.adapter == null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewPager.defaultFocusHighlightEnabled = false
        }
        viewPager.adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems!!)
    }

    applyData(viewPager.adapter!!, oldItems, newItems)
}

@BindingAdapter(value = ["selectedPage", "positionAttrChanged"], requireAll = false)
fun setSelectedPosition(viewPager: ViewPager2, selectedPage: Int?, changeListener: InverseBindingListener?) {
    selectedPage?.let {
        viewPager.setCurrentItem(it, false)
        customOnPageChangeCallback.onPageChangedCallback = {
            changeListener?.onChange()
        }
        viewPager.unregisterOnPageChangeCallback(customOnPageChangeCallback)
        viewPager.registerOnPageChangeCallback(customOnPageChangeCallback)
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

    recyclerView.tag?.let {
        @Suppress("UNCHECKED_CAST")
        oldItems = it as? MutableList<ItemType>
    }

    if (oldItems == null) {
        oldItems = mutableListOf()
        recyclerView.tag = oldItems
    }

    if (recyclerView.adapter == null) {

        if (recyclerView.layoutManager == null) {
            val mLayoutManager: RecyclerView.LayoutManager = dataBindingRecyclerViewConfig.layoutManager ?:
                LinearLayoutManager(recyclerView.context, dataBindingRecyclerViewConfig.orientation, false)
            recyclerView.layoutManager = mLayoutManager

            dataBindingRecyclerViewConfig.onScrollListener?.let { onLoadMoreHandler ->
                recyclerView.clearOnScrollListeners()
                recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener(mLayoutManager, visibleThreshold) {
                    override fun onLoadMore(currentPage: Int) {
                        onLoadMoreHandler(currentPage)
                    }
                })
            }
        }
        recyclerView.itemAnimator = DefaultItemAnimator()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.touchscreenBlocksFocus = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recyclerView.defaultFocusHighlightEnabled = false
        }

        recyclerView.adapter = dataBindingRecyclerViewConfig.createAdapter(oldItems!!)
    }

    val adapter = recyclerView.adapter!!
    applyData(adapter, oldItems, newItems)
}

@Suppress("UNCHECKED_CAST")
private fun<ItemType : Any, BindingType : ViewDataBinding> DataBindingRecyclerViewConfig<BindingType>.createAdapter(items: List<ItemType>): RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder> {
    return DataBindingRecyclerAdapter(
        items = items,
        lifecycleOwner = this.lifecycleOwner,
        layoutId = this.layoutId,
        itemId = this.itemId,
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

data class DataBindingRecyclerViewConfig<BindingType : ViewDataBinding>(
    val layoutId: Int,
    val itemId: Int,
    val orientation: Int = RecyclerView.VERTICAL,
    val lifecycleOwner: LifecycleOwner? = null,
    val realisation: DataBindingAdapter<BindingType>? = null,
    val onItemClickListener: OnRecyclerItemClickListener? = null,
    val onItemLongClickListener: OnRecyclerItemLongClickListener? = null,
    val onItemDoubleClickListener: OnRecyclerItemDoubleClickListener? = null,
    val onScrollListener: ((Int) -> Unit)? = null,
    val layoutManager: RecyclerView.LayoutManager? = null
) {

    class DataBindingRecyclerViewConfigBuilder<I : Any, BT : ViewDataBinding> {
        var layoutId: Int? = null
        var itemId: Int? = null
        var lifecycleOwner: LifecycleOwner? = null
        var onItemCreate: ((BT) -> Unit)? = null
        var onItemUnbind: ((BT) -> Unit)? = null
        var orientation: Int = RecyclerView.VERTICAL
        var onItemBind: ((BT, Int) -> Unit)? = null
        var onLoadMore: ((Int) -> Unit)? = null
        var onItemClick:((I, Int) -> Unit)? = null
        var onItemDoubleClicked: ((I, Int) -> Unit)? = null
        var onItemLongClickListener: ((I, Int) -> Unit)? = null
        var layoutManager: RecyclerView.LayoutManager? = null

        fun build(): DataBindingRecyclerViewConfig<BT> {
            return DataBindingRecyclerViewConfig(
                layoutId = layoutId ?: -1,
                itemId = itemId ?: throw NullPointerException("DataBindingRecyclerViewConfig::itemId must not be null"),
                lifecycleOwner = lifecycleOwner,
                orientation = orientation,
                layoutManager = layoutManager,
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
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>().apply(block).build()
}

inline fun recyclerMultiConfig(block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>.() -> Unit): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>().apply(block).build()
}

class DataBindingRecyclerAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private val items: List<ItemType>,
    private val layoutId: Int,
    private val itemId: Int,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val realisation: DataBindingAdapter<BindingType>? = null,
    private val onItemClickListener: OnRecyclerItemClickListener? = null,
    private val onItemLongClickListener: OnRecyclerItemLongClickListener? = null,
    private val onItemDoubleClickListener: OnRecyclerItemDoubleClickListener? = null
) : RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder>(),
    DataBindingAdapter<BindingType> {


    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return (items.getOrNull(position) as? IBindingModel)?.layoutResId ?: layoutId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = DataBindingUtil.inflate<BindingType>(LayoutInflater.from(parent.context), viewType, parent, false)
        onCreate(binding)
        val result = BindingViewHolder(binding)
        if (onItemClickListener != null || onItemDoubleClickListener != null) {
            binding.root.setOnClickListener(object : DoubleClickListener() {
                override fun onSingleClick(view: View) {
                    val position = result.absoluteAdapterPosition
                    onItemClickListener?.onItemClicked(items[position], position)
                }

                override fun onDoubleClick(view: View) {
                    val position = result.absoluteAdapterPosition
                    onItemDoubleClickListener?.onItemDoubleClicked(items[position], position)
                }
            })
        }

        onItemLongClickListener?.let {
            binding.root.setOnLongClickListener { _ ->
                val position = result.absoluteAdapterPosition
                it.onItemLongClicked(items[position], position)
                false
            }
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        if (itemId != -1) {
            holder.binding.setVariable(itemId, items[holder.absoluteAdapterPosition])
            holder.binding.lifecycleOwner = lifecycleOwner
        }
        onBind(holder.binding as BindingType, holder.absoluteAdapterPosition)
        holder.binding.executePendingBindings()
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

abstract class DoubleClickListener : View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        } else {
            onSingleClick(v)
        }

        lastClickTime = clickTime
    }

    abstract fun onSingleClick(view: View)

    abstract fun onDoubleClick(view: View)

    companion object {

        private val DOUBLE_CLICK_TIME_DELTA = ViewConfiguration.getDoubleTapTimeout().toLong()
    }
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