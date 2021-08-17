package com.rasalexman.easyrecyclerbinding

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

/**
 * Create simple binding for given [VB] Generic class
 *
 * @param layoutId - layout resource id for data binding
 * @param container - parent container
 * @param attachToParent - is need to attach to parent
 */
fun <VB : ViewDataBinding> LayoutInflater.createBinding(
    layoutId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false,
    findLifeCycle: Boolean = false
): VB {
    return DataBindingUtil.inflate<VB>(this, layoutId, container, attachToParent).apply {
        if (findLifeCycle) lifecycleOwner = root.context.getOwner<LifecycleOwner>()
    }
}

/**
 * Create Data binding and return it root view
 *
 * @param layoutId - layout resource id for data binding
 * @param container - parent container
 * @param attachToParent - is need to attach to parent
 */
fun <VB : ViewDataBinding> LayoutInflater.createBindingView(
    layoutId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false
): View {
    return createBinding<VB>(layoutId, container, attachToParent).root
}

fun <VB : ViewDataBinding, VM : ViewModel> Fragment.createBindingWithViewModel(
    layoutId: Int,
    viewModel: VM,
    viewModelBRId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false
): VB {
    return layoutInflater.createBinding<VB>(layoutId, container, attachToParent, false).also {
        it.lifecycleOwner = viewLifecycleOwner
        it.setVariable(viewModelBRId, viewModel)
        it.executePendingBindings()
    }
}

fun <I : Any, BT : ViewDataBinding> Fragment.createRecyclerConfig(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit
): DataBindingRecyclerViewConfig<BT> {
    return getFragmentRecyclerConfigBuilder(block).build()
}

fun <I : Any, BT : ViewDataBinding> Fragment.createPagingRecyclerConfig(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit
): DataBindingRecyclerViewConfig<BT> {
    return getFragmentRecyclerConfigBuilder(block).run {
        adapterType = BindingAdapterType.PAGING
        build()
    }
}

fun Fragment.createRecyclerMultiConfig(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>.() -> Unit
): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return getFragmentRecyclerConfigBuilder(block).build()
}

fun Fragment.createPagingRecyclerMultiConfig(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>.() -> Unit
): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return getFragmentRecyclerConfigBuilder(block).run {
        adapterType = BindingAdapterType.PAGING
        build()
    }
}

private fun <I : Any, BT : ViewDataBinding> Fragment.getFragmentRecyclerConfigBuilder(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit
): DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT> {
    return getRecyclerConfigBuilder(block)
        .also {
            it.lifecycleOwner = viewLifecycleOwner
        }
}

fun <I : Any, BT : ViewDataBinding> getRecyclerConfigBuilder(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit
): DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>().apply(block)
}

fun Context.findPrimaryFragment(): Fragment? {
    return (this as? FragmentActivity)?.run {
        val frag = supportFragmentManager.fragments
        frag.lastOrNull()?.childFragmentManager?.primaryNavigationFragment
    }
}

/**
 * Inline function to retrieve [Context] owners
 */
inline fun <reified T> Context.getOwner(): T? {
    var context: Context? = this
    while (context !is T) {
        context = (context as? ContextWrapper)?.baseContext
    }
    return context
}