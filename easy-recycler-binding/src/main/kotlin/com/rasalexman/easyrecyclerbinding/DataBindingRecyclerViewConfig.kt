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
    val onItemPosClickListener: OnRecyclerItemClickListener? = null,
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
    val removeAndRecycleExistingViews: Boolean = false,
    val isSwapAdapter: Boolean = true
) {

    @Suppress("unchecked_cast")
    class DataBindingRecyclerViewConfigBuilder<I : Any, BT : ViewDataBinding> {
        /**
         * Recycler view item view holder layout id
         */
        var layoutId: Int? = null

        /**
         * Data Binding data item id. Used in tag 'variable'
         */
        var itemId: Int? = null

        /**
         * Item double click delay
         */
        var doubleClickDelayTime: Long = 150L

        /**
         * Type of Rv Adapter [BindingAdapterType]
         */
        var adapterType: BindingAdapterType = BindingAdapterType.STANDARD

        /**
         * Is need to consume long click on view holder
         */
        var consumeLongClick: Boolean = true

        /**
         * Current lifecycle owner. if it does not set config try to find parent one
         */
        var lifecycleOwner: LifecycleOwner? = null
        var onItemCreate: ((BT) -> Unit)? = null
        var onItemUnbind: ((BT) -> Unit)? = null
        var orientation: Int = RecyclerView.VERTICAL
        var onItemBind: ((BT, Int) -> Unit)? = null
        var onLoadMore: ((Int) -> Unit)? = null
        var onItemPosClick: ((I, Int) -> Unit)? = null
        var onItemClick: ((I) -> Unit)? = null
        var onModelBind: ((I, Int) -> Unit)? = null
        var onItemDoubleClicked: ((I, Int) -> Unit)? = null
        var onItemLongClickListener: ((I, Int) -> Unit)? = null

        /**
         * Recycler layout manager. If it isn't set, the LinearLayout is installed
         */
        var layoutManager: RecyclerView.LayoutManager? = null
        var onScrollListener: RecyclerView.OnScrollListener? = null
        var onPageSelectedListener: ((Int) -> Unit)? = null
        var onPageScrollStateListener: ((Int) -> Unit)? = null
        var itemAnimator: RecyclerView.ItemAnimator? = null
        var itemDecorator: List<RecyclerView.ItemDecoration>? = null
        var diffUtilCallback: DiffCallback<*>? = DiffCallback<I>()
        var diffItemUtilCallback: DiffItemsCallback<I>? = null
        var hasFixedSize: Boolean = true
        var isLifecyclePending: Boolean = true
        var isReverseLayout: Boolean = false
        var onAdapterAdded: ((RecyclerView.Adapter<*>) -> Unit)? = null
        var stateFooterAdapter: DataBindingLoadStateAdapter<ILoadStateModel>? =
            null
        var stateHeaderAdapter: DataBindingLoadStateAdapter<ILoadStateModel>? =
            null

        /**
         * If set to true, RecyclerView will recycle all existing Views.
         * If adapters have stable ids and/or you want to animate the disappearing views,
         * you may prefer to set this to false.
         */
        var removeAndRecycleExistingViews: Boolean = false

        /**
         * Swaps the current adapter with the provided one by calls [RecyclerView.swapAdapter].
         * It is similar to setAdapter(RecyclerView.Adapter) but assumes existing adapter
         * and the new adapter uses the same RecyclerView.ViewHolder
         * and does not clear the RecycledViewPool.
         * Note that it still calls onAdapterChanged callbacks.
         *
         * If it set to 'false' config calls [RecyclerView.setAdapter]
         * Default: true
         */
        var isSwapAdapter: Boolean = true

        private val isHasRealisationCallbacks: Boolean
            get() = onItemCreate != null || onItemBind != null
                    || onItemUnbind != null || onModelBind != null

        @Throws(NullPointerException::class)
        fun build(): DataBindingRecyclerViewConfig<BT> {
            // check for itemId !!!
            val currentItemId = itemId
                ?: throw NullPointerException("DataBindingRecyclerViewConfig::itemId must not be null")
            // current layoutId
            val currentLayoutId = layoutId ?: -1

            val onItemPosClickHandler = createOnItemPosClickHandler()
            val onItemClickHandler = createOnItemClickHandler()
            val onItemDoubleClickHandler = createOnItemDoubleClickHandler()
            val onItemLongClickHandler = createOnItemLongClickHandler()
            val realisationHandlers = createRealisationHandlers()

            // create a full config object
            return DataBindingRecyclerViewConfig(
                layoutId = currentLayoutId,
                itemId = currentItemId,
                adapterType = this.adapterType,
                lifecycleOwner = this.lifecycleOwner,
                orientation = this.orientation,
                doubleClickDelayTime = this.doubleClickDelayTime,
                consumeLongClick = this.consumeLongClick,
                layoutManager = this.layoutManager,
                onScrollListener = onLoadMore,
                onPageSelectedListener = this.onPageSelectedListener,
                onPageScrollStateListener = this.onPageScrollStateListener,
                recyclerOnScrollListener = onScrollListener,
                itemAnimator = this.itemAnimator,
                itemDecorator = this.itemDecorator,
                diffUtilCallback = this.diffUtilCallback,
                diffItemsUtilCallback = this.diffItemUtilCallback,
                hasFixedSize = this.hasFixedSize,
                isLifecyclePending = this.isLifecyclePending,
                isReverseLayout = this.isReverseLayout,

                stateFooterAdapter = this.stateFooterAdapter,
                stateHeaderAdapter = this.stateHeaderAdapter,

                realisation = realisationHandlers,
                onItemPosClickListener = onItemPosClickHandler,
                onItemClickListener = onItemClickHandler,
                onItemDoubleClickListener = onItemDoubleClickHandler,
                onItemLongClickListener = onItemLongClickHandler,
                onAdapterAdded = this.onAdapterAdded,
                removeAndRecycleExistingViews = this.removeAndRecycleExistingViews,
                isSwapAdapter = this.isSwapAdapter
            )
        }

        private fun createRealisationHandlers(): DataBindingAdapter<BT>? {
            return isHasRealisationCallbacks.takeIf { it }?.run {
                object : DataBindingAdapter<BT> {
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
                }
            }
        }

        private fun createOnItemPosClickHandler(): OnRecyclerItemClickListener? {
            return onItemPosClick?.let { currentClickCallback ->
                object : OnRecyclerItemClickListener {
                    override fun <T : Any> onItemClicked(item: T?) = Unit
                    override fun <T : Any> onItemClicked(item: T?, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            currentClickCallback.invoke(it, position)
                        }
                    }
                }
            }
        }

        private fun createOnItemClickHandler(): OnRecyclerItemClickListener? {
            return onItemClick?.let { currentClickCallback ->
                object : OnRecyclerItemClickListener {
                    override fun <T : Any> onItemClicked(item: T?, position: Int) = Unit
                    override fun <T : Any> onItemClicked(item: T?) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            currentClickCallback.invoke(it)
                        }
                    }
                }
            }
        }

        private fun createOnItemDoubleClickHandler(): OnRecyclerItemDoubleClickListener? {
            return onItemDoubleClicked?.let { currentDoubleCallback ->
                object : OnRecyclerItemDoubleClickListener {
                    override fun <T : Any> onItemDoubleClicked(item: T?, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            currentDoubleCallback.invoke(it, position)
                        }
                    }
                }
            }
        }

        private fun createOnItemLongClickHandler(): OnRecyclerItemLongClickListener? {
            return onItemLongClickListener?.let { currentLongCallback ->
                object : OnRecyclerItemLongClickListener {
                    override fun <T : Any> onItemLongClicked(item: T?, position: Int) {
                        val selectedItem = item as? I
                        selectedItem?.let {
                            currentLongCallback.invoke(it, position)
                        }
                    }
                }
            }
        }
    }
}