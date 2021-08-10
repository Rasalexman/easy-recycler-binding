package com.rasalexman.easyrecyclerbinding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class ErbAdapter<ItemType : Any, BindingType : ViewDataBinding>(
    private var items: List<ItemType>,
    private val layoutId: Int,
    private val itemId: Int,
    private val doubleClickDelayTime: Long = 150L,
    private val consumeLongClick: Boolean = true,
    private val isLifecyclePending: Boolean = false,
    private val lifecycleOwner: LifecycleOwner? = null,
    private val realisation: DataBindingAdapter<BindingType>? = null,
    private val onItemClickListener: OnRecyclerItemClickListener? = null,
    private val onItemLongClickListener: OnRecyclerItemLongClickListener? = null,
    private val onItemDoubleClickListener: OnRecyclerItemDoubleClickListener? = null
) : IErbAdapter<ItemType, BindingType> {

    private var parentFragmentLifecycleOwner: WeakReference<LifecycleOwner>? = null
    private var layoutInflater: WeakReference<LayoutInflater>? = null

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
        val localBinding = holder.binding
        if (itemId != -1) {
            localBinding.setVariable(itemId, items[absolutePosition])
            if (localBinding.lifecycleOwner == null && !isLifecyclePending) {
                localBinding.executePendingBindings()
            }
        }
        onBind(localBinding as BindingType, absolutePosition)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: BindingViewHolder) {
        onUnbind(holder.binding as BindingType)
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
}

class BindingViewHolder(vb: ViewDataBinding) : RecyclerView.ViewHolder(vb.root) {
    private val currentBinding: WeakReference<ViewDataBinding> = WeakReference(vb)
    val binding: ViewDataBinding
        get() = currentBinding.get()
            ?: throw NullPointerException("There is no binding for ViewHolder $this")
}

interface IErbAdapter<ItemType : Any, BindingType : ViewDataBinding> :
    DataBindingAdapter<BindingType>, ItemsBinderAdapter<ItemType> {

    fun getItemViewType(position: Int): Int
    fun getItemCount(): Int
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder
    fun onViewRecycled(holder: BindingViewHolder)
    fun onBindViewHolder(holder: BindingViewHolder, position: Int)
}