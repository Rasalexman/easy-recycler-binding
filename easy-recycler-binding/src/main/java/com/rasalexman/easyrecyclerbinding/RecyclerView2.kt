package com.rasalexman.easyrecyclerbinding


import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

@BindingAdapter(value = ["items", "rv_multi_config", "visibleThreshold"], requireAll = false)
fun setupRecyclerMultiView(
    recyclerView: RecyclerView,
    newItems: List<IBindingModel>?,
    dataBindingRecyclerViewConfig: DataMultiBindingRecyclerViewConfig?,
    visibleThreshold: Int = 5
) {

    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    var oldItems: MutableList<IBindingModel>? = null

    recyclerView.tag?.let {
        @Suppress("UNCHECKED_CAST")
        oldItems = it as? MutableList<IBindingModel>
    }

    if (oldItems == null) {
        oldItems = mutableListOf()
        recyclerView.tag = oldItems
    }

    if (recyclerView.adapter == null) {

        if (recyclerView.layoutManager == null) {
            val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(recyclerView.context)
            recyclerView.layoutManager = mLayoutManager

            dataBindingRecyclerViewConfig.onScrollListener.let { onLoadMoreHandler ->
                recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener(mLayoutManager, visibleThreshold) {
                    override fun onLoadMore(currentPage: Int) {
                        onLoadMoreHandler?.invoke(currentPage)
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

        recyclerView.adapter = DataMultiBindingRecyclerAdapter(
            items = oldItems,
            lifecycleOwner = dataBindingRecyclerViewConfig.lifecycleOwner,
            itemId = dataBindingRecyclerViewConfig.itemId,
            realisation = dataBindingRecyclerViewConfig.realisation,
            onItemClickListener = dataBindingRecyclerViewConfig.onItemClickListener,
            onItemDoubleClickListener = dataBindingRecyclerViewConfig.onItemDoubleClickListener,
            onItemLongClickListener = dataBindingRecyclerViewConfig.onItemLongClickListener
        )
    }

    val adapter = recyclerView.adapter!!
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

data class DataMultiBindingRecyclerViewConfig(
    val itemId: Int,
    val lifecycleOwner: LifecycleOwner? = null,
    val realisation: DataBindingAdapter<ViewDataBinding>? = null,
    val onItemClickListener: OnRecyclerMultiItemClickListener? = null,
    val onItemLongClickListener: OnRecyclerMultiItemLongClickListener? = null,
    val onItemDoubleClickListener: OnRecyclerMultiItemDoubleClickListener? = null,
    val onScrollListener: ((Int) -> Unit)? = null
) {

    class DataMultiBindingRecyclerViewConfigBuilder {
        var itemId: Int? = null
        var lifecycleOwner: LifecycleOwner? = null
        var onItemCreate: ((ViewBinding) -> Unit)? = null
        var onItemUnbind: ((ViewBinding) -> Unit)? = null
        var onItemBind: ((ViewBinding, Int) -> Unit)? = null
        var onLoadMore: ((Int) -> Unit)? = null
        var onItemClick: ((IBindingModel, Int) -> Unit)? = null
        var onItemDoubleClicked: ((IBindingModel, Int) -> Unit)? = null
        var onItemLongClickListener: ((IBindingModel, Int) -> Unit)? = null

        fun build(): DataMultiBindingRecyclerViewConfig {
            return DataMultiBindingRecyclerViewConfig(
                itemId = itemId ?: throw NullPointerException("DataBindingRecyclerViewConfig::itemId must not be null"),
                lifecycleOwner = lifecycleOwner,
                realisation = object : DataBindingAdapter<ViewDataBinding> {
                    override fun onCreate(binding: ViewDataBinding) {
                        onItemCreate?.invoke(binding)
                    }

                    override fun onBind(binding: ViewDataBinding, position: Int) {
                        onItemBind?.invoke(binding, position)
                    }

                    override fun onUnbind(binding: ViewDataBinding) {
                        onItemUnbind?.invoke(binding)
                    }
                },
                onItemClickListener = object : OnRecyclerMultiItemClickListener {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : IBindingModel> onItemClicked(item: T, position: Int) {
                        val selectedItem = item as? T
                        selectedItem?.let {
                            onItemClick?.invoke(it, position)
                        }
                    }
                },
                onItemDoubleClickListener = object : OnRecyclerMultiItemDoubleClickListener {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : IBindingModel> onItemDoubleClicked(item: T, position: Int) {
                        val selectedItem = item as? T
                        selectedItem?.let {
                            onItemDoubleClicked?.invoke(it, position)
                        }
                    }
                },
                onItemLongClickListener = object : OnRecyclerMultiItemLongClickListener {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : IBindingModel> onItemLongClicked(item: T, position: Int) {
                        val selectedItem = item as? T
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

interface OnRecyclerMultiItemClickListener {
    fun <T : IBindingModel> onItemClicked(item: T, position: Int)
}

interface OnRecyclerMultiItemDoubleClickListener {
    fun <T : IBindingModel> onItemDoubleClicked(item: T, position: Int)
}

interface OnRecyclerMultiItemLongClickListener {
    fun <T : IBindingModel> onItemLongClicked(item: T, position: Int)
}

inline fun recyclerMultiConfig(block: DataMultiBindingRecyclerViewConfig.DataMultiBindingRecyclerViewConfigBuilder.() -> Unit): DataMultiBindingRecyclerViewConfig {
    return DataMultiBindingRecyclerViewConfig.DataMultiBindingRecyclerViewConfigBuilder().apply(block).build()
}

class DataMultiBindingRecyclerAdapter(
    private val items: List<IBindingModel>?,
    private val itemId: Int,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val realisation: DataBindingAdapter<ViewDataBinding>? = null,
    private val onItemClickListener: OnRecyclerMultiItemClickListener? = null,
    private val onItemLongClickListener: OnRecyclerMultiItemLongClickListener? = null,
    private val onItemDoubleClickListener: OnRecyclerMultiItemDoubleClickListener? = null
) : RecyclerView.Adapter<DataMultiBindingRecyclerAdapter.BindingViewHolder>(),
    DataBindingAdapter<ViewDataBinding> {


    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return items!![position].layoutResId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent, false)
        onCreate(binding)
        val result = BindingViewHolder(binding)
        if (onItemClickListener != null || onItemDoubleClickListener != null) {
            binding.root.setOnClickListener(object : DoubleClickListener() {
                override fun onSingleClick(view: View) {
                    val position = result.absoluteAdapterPosition
                    onItemClickListener?.onItemClicked(items!![position], position)
                }

                override fun onDoubleClick(view: View) {
                    val position = result.absoluteAdapterPosition
                    onItemDoubleClickListener?.onItemDoubleClicked(items!![position], position)
                }
            })
        }

        onItemLongClickListener?.let {
            binding.root.setOnLongClickListener { _ ->
                val position = result.absoluteAdapterPosition
                it.onItemLongClicked(items!![position], position)
                false
            }
        }

        return result
    }

    override fun onBindViewHolder(@NonNull holder: BindingViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (itemId != -1) {
            holder.binding.setVariable(itemId, items!![holder.absoluteAdapterPosition])
            holder.binding.lifecycleOwner = lifecycleOwner
        }
        @Suppress("UNCHECKED_CAST")
        onBind(holder.binding, holder.absoluteAdapterPosition)
        holder.binding.executePendingBindings()
    }

    override fun onViewRecycled(holder: BindingViewHolder) {
        onUnbind(holder.binding)
        holder.binding.unbind()
        holder.binding.lifecycleOwner = null
        super.onViewRecycled(holder)
    }

    override fun onCreate(binding: ViewDataBinding) {
        realisation?.onCreate(binding)
    }

    override fun onBind(binding: ViewDataBinding, position: Int) {
        realisation?.onBind(binding, position)
    }

    override fun onUnbind(binding: ViewDataBinding) {
        realisation?.onUnbind(binding)
    }

    class BindingViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}
