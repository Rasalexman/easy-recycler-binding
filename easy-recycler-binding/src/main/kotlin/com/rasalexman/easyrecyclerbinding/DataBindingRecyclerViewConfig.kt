package com.rasalexman.easyrecyclerbinding

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.rasalexman.easyrecyclerbinding.adapters.DataBindingAdapter
import com.rasalexman.easyrecyclerbinding.adapters.DataBindingLoadStateAdapter
import com.rasalexman.easyrecyclerbinding.common.BindingAdapterType

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
    val onPageSelectedListener: ((Int) -> Unit)? = null,
    val onPageScrollStateListener: ((Int) -> Unit)? = null,
    val layoutManager: RecyclerView.LayoutManager? = null,
    val isReverseLayout: Boolean = false,
    val recyclerOnScrollListener: RecyclerView.OnScrollListener? = null,
    val itemAnimator: RecyclerView.ItemAnimator? = null,
    val itemDecorator: List<RecyclerView.ItemDecoration>? = null,
    val diffUtilCallback: DiffCallback<*>?,
    val diffItemsUtilCallback: DiffItemsCallback<*>?,
    val hasFixedSize: Boolean = true,
    val isLifecyclePending: Boolean = true,
    val stateFooterAdapter: DataBindingLoadStateAdapter<ILoadStateModel>? = null,
    val stateHeaderAdapter: DataBindingLoadStateAdapter<ILoadStateModel>? = null,
    val onAdapterAdded: ((RecyclerView.Adapter<*>) -> Unit)? = null,
) {

    @Suppress("unchecked_cast")
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
        var onPageSelectedListener: ((Int) -> Unit)? = null
        var onPageScrollStateListener: ((Int) -> Unit)? = null
        var itemAnimator: RecyclerView.ItemAnimator? = null
        var itemDecorator: List<RecyclerView.ItemDecoration>? = null
        var diffUtilCallback: DiffCallback<*>? = null
        var diffItemUtilCallback: DiffItemsCallback<I>? = null
        var hasFixedSize: Boolean = true
        var isLifecyclePending: Boolean = true
        var isReverseLayout: Boolean = false
        var onAdapterAdded: ((RecyclerView.Adapter<*>) -> Unit)? = null

        var stateFooterAdapter: DataBindingLoadStateAdapter<ILoadStateModel>? =
            null
        var stateHeaderAdapter: DataBindingLoadStateAdapter<ILoadStateModel>? =
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
                onPageSelectedListener = onPageSelectedListener,
                onPageScrollStateListener = onPageScrollStateListener,
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