package com.rasalexman.easyrecyclerbinding.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.rasalexman.easyrecyclerbinding.*
import com.rasalexman.easyrecyclerbinding.common.ViewClickersListener
import java.lang.ref.WeakReference

class ErbAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private var items: List<ItemType>,
    private val layoutId: Int,
    private val itemId: Int,
    private val doubleClickDelayTime: Long = 200L,
    private val consumeLongClick: Boolean = true,
    private val isLifecyclePending: Boolean = true,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val realisation: DataBindingAdapter<BindingType>? = null,
    private val onItemPosClickListener: OnRecyclerItemClickListener? = null,
    private val onItemClickListener: OnRecyclerItemClickListener? = null,
    private val onItemLongClickListener: OnRecyclerItemLongClickListener? = null,
    private val onItemDoubleClickListener: OnRecyclerItemDoubleClickListener? = null
) : IErbAdapter<ItemType, BindingType> {

    var onGetItemHandler: ((Int) -> ItemType?)? = null
    var onGetItemsCountHandler: (() -> Int)? = null

    private val hasClicksListeners: Boolean = onItemPosClickListener != null
            || onItemClickListener != null
            || onItemLongClickListener != null
            || onItemDoubleClickListener != null

    private var parentFragmentLifecycleOwner: WeakReference<LifecycleOwner>? = null
    private var layoutInflater: WeakReference<LayoutInflater>? = null

    override fun getItemCount(): Int {
        return onGetItemsCountHandler?.invoke() ?: items.size
    }

    override fun setAdapterItems(items: List<ItemType>) {
        this.items = items
    }

    override fun getAdapterItems(): List<ItemType> {
        return this.items
    }

    override fun getAdapterItem(position: Int): ItemType? {
        return onGetItemHandler?.invoke(position) ?: this.items.getOrNull(position)
    }

    override fun getItemViewType(position: Int): Int {
        val viewType = layoutId.takeIf { it != -1 }
            ?: (getAdapterItem(position) as? IBindingModel)?.layoutResId
        return viewType ?: throw IllegalStateException(
            "layoutId is not set for viewHolder with item data position = $position"
        )
    }

    private fun getLifecycleOwner(view: View): LifecycleOwner? {
        return parentFragmentLifecycleOwner?.get() ?: try {
            view.findFragment<Fragment>().viewLifecycleOwner
        } catch (e: Exception) {
            view.context.getOwner<LifecycleOwner>()
        }?.also {
            parentFragmentLifecycleOwner = WeakReference(it)
        }
    }

    private fun getLayoutInflater(context: Context): LayoutInflater {
        return layoutInflater?.get() ?: LayoutInflater.from(context).apply {
            layoutInflater = WeakReference(this)
        }
    }

    private fun setupLifecycleOwner(binding: ViewDataBinding, parent: ViewGroup) {
        val bindingLifecycleOwner = binding.lifecycleOwner
        if (this.lifecycleOwner == null) {
            if (bindingLifecycleOwner == null && isLifecyclePending) {
                val parentLifecycleOwner = getLifecycleOwner(parent)
                binding.lifecycleOwner = parentLifecycleOwner
            }
        } else {
            if (bindingLifecycleOwner == null) {
                binding.lifecycleOwner = this.lifecycleOwner
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = getLayoutInflater(parent.context).createBinding<BindingType>(viewType, parent)

        setupLifecycleOwner(binding, parent)
        val holder = BindingViewHolder(binding)
        onCreate(binding)

        if (hasClicksListeners) {
            val clickListener = setupHolderClickListeners(holder)
            binding.root.apply {
                if (hasClickListeners()) {
                    setOnClickListener(clickListener)
                }
                onItemLongClickListener?.let {
                    setOnLongClickListener(clickListener)
                }
            }
        }

        return holder
    }

    private fun hasClickListeners(): Boolean {
        return onItemPosClickListener != null || onItemClickListener != null || onItemDoubleClickListener != null
    }

    private fun setupHolderClickListeners(holder: BindingViewHolder): ViewClickersListener {
        return object : ViewClickersListener(
            hasDoubleClickListener = onItemDoubleClickListener != null,
            doubleClickDelayTime = doubleClickDelayTime,
            consumeLongClick = consumeLongClick
        ) {
            private var currentHolder: BindingViewHolder = holder

            override fun onSingleClicked() {
                onItemPosClickListener?.let {
                    val (position, item) = getItemAndPosition()
                    it.onItemClicked(item, position)
                }
                onItemClickListener?.let {
                    it.onItemClicked(getItemAndPosition().second)
                }
            }

            override fun onDoubleClicked() {
                onItemDoubleClickListener?.let {
                    val (position, item) = getItemAndPosition()
                    it.onItemDoubleClicked(item, position)
                }
            }

            override fun onLongClicked() {
                onItemLongClickListener?.let {
                    val (position, item) = getItemAndPosition()
                    it.onItemLongClicked(item, position)
                }
            }

            private fun getItemAndPosition(): Pair<Int, ItemType?> {
                val adapterPosition = currentHolder.bindingAdapterPosition
                val position = getViewHolderPosition(currentHolder, adapterPosition)
                val item = getAdapterItem(position)
                return position to item
            }
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        if (itemId != -1) {
            val absolutePosition = getViewHolderPosition(holder, position)
            val item = getAdapterItem(absolutePosition)
            if (item != null) {
                val localBinding = DataBindingUtil.getBinding<BindingType>(holder.itemView)
                if (localBinding != null) {
                    localBinding.setVariable(itemId, item)
                    onBindItem(item, absolutePosition)
                    onBind(localBinding, absolutePosition)
                    if (isLifecyclePending) {
                        localBinding.executePendingBindings()
                    }
                }
            }
        } else {
            throw IllegalStateException("`itemId` is not set for RecyclerDataBindingAdapter")
        }
    }

    private fun getViewHolderPosition(holder: BindingViewHolder, position: Int): Int {
        val absolutePosition = holder.absoluteAdapterPosition
        val itemsCount = getItemCount()
        return if (itemsCount < absolutePosition) {
            position
        } else {
            absolutePosition
        }
    }

    override fun onViewRecycled(holder: BindingViewHolder) {
        val localBinding = DataBindingUtil.getBinding<BindingType>(holder.itemView)
        localBinding?.let(::onUnbind)
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

    override fun clearAdapter() {
        onGetItemHandler = null
        onGetItemsCountHandler = null
        layoutInflater?.clear()
        parentFragmentLifecycleOwner?.clear()
        layoutInflater = null
        parentFragmentLifecycleOwner = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> onBindItem(item: T?, position: Int) {
        realisation?.onBindItem(item, position)
    }
}

class BindingViewHolder(vb: ViewDataBinding) : RecyclerView.ViewHolder(vb.root)

interface IErbAdapter<ItemType : Any, BindingType : ViewDataBinding> :
    DataBindingAdapter<BindingType>, ItemsBinderAdapter<ItemType> {

    fun getAdapterItem(@IntRange(from = 0) position: Int): ItemType?
    fun getItemViewType(position: Int): Int
    fun getItemCount(): Int
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder
    fun onViewRecycled(holder: BindingViewHolder)
    fun onBindViewHolder(holder: BindingViewHolder, position: Int)

    fun clearAdapter()
}

interface DataBindingAdapter<BindingType : ViewDataBinding> {
    fun onCreate(binding: BindingType)
    fun onBind(binding: BindingType, position: Int)
    fun <T : Any> onBindItem(item: T?, position: Int)
    fun onUnbind(binding: BindingType)
}

interface ItemsBinderAdapter<ItemType : Any> {
    fun setAdapterItems(items: List<ItemType>)
    fun getAdapterItems(): List<ItemType>
}