@file:Suppress("unused")

package com.rasalexman.easyrecyclerbinding

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.rasalexman.easyrecyclerbinding.adapters.DataBindingLoadStateAdapter
import com.rasalexman.easyrecyclerbinding.common.BindingAdapterType

typealias ItemsConfig<I> = DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, ViewDataBinding>
typealias ItemsBindingConfig<I, B> = DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, B>

fun <I : ILoadStateModel> createStateAdapter(
    item: I,
    lifecycleOwner: LifecycleOwner? = null,
    block: ItemsConfig<I>.() -> Unit
): DataBindingLoadStateAdapter<I> {
    return ItemsConfig<I>().apply(block).also {
        it.adapterType = BindingAdapterType.LOADING
        it.lifecycleOwner = lifecycleOwner
    }.build().asLoadingAdapter(item)
}

fun <I : ILoadStateModel> Fragment.createStateLoadingAdapter(
    item: I,
    block: ItemsConfig<I>.() -> Unit
): DataBindingLoadStateAdapter<I> {
    return createStateAdapter(item, viewLifecycleOwner, block)
}

@Suppress("UNCHECKED_CAST")
private fun <I : ILoadStateModel> DataBindingRecyclerViewConfig<ViewDataBinding>.asLoadingAdapter(
    item: I
): DataBindingLoadStateAdapter<I> {
    return (createAdapter(items = listOf(item)) as DataBindingLoadStateAdapter<I>)
        .apply {
            loadState = item.loadState
        }
}

fun <I : Any, BT : ViewDataBinding> recyclerConfig(block: ItemsBindingConfig<I, BT>.() -> Unit): DataBindingRecyclerViewConfig<BT> {
    return getRecyclerConfigBuilder(block).build().also {
        if (it.layoutId == -1) {
            throw IllegalStateException("layoutId is not set for viewHolders adapter")
        }
    }
}

fun recyclerMultiConfig(block: ItemsConfig<IBindingModel>.() -> Unit): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return getRecyclerConfigBuilder(block).build()
}

fun <I : Any, BT : ViewDataBinding> pagingConfig(block: ItemsBindingConfig<I, BT>.() -> Unit): DataBindingRecyclerViewConfig<BT> {
    return getRecyclerConfigBuilder(block).run {
        adapterType = BindingAdapterType.PAGING
        build()
    }.also {
        if (it.layoutId == -1) {
            throw IllegalStateException("layoutId is not set for viewHolders adapter")
        }
    }
}

fun pagingMultiConfig(block: ItemsConfig<IBindingModel>.() -> Unit): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return getRecyclerConfigBuilder(block).run {
        adapterType = BindingAdapterType.PAGING
        build()
    }
}

internal fun <I : Any, BT : ViewDataBinding> getRecyclerConfigBuilder(
    block: ItemsBindingConfig<I, BT>.() -> Unit
): ItemsBindingConfig<I, BT> {
    return ItemsBindingConfig<I, BT>().apply(block)
}