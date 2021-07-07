package com.rasalexman.easyrecyclerbinding

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

/**
 * Create simple binding for given [VB] Generic class
 *
 * @param layoutId - layout resource id for data binding
 * @param container - parent container
 * @param attachToParent - is need to attach to parent
 */
fun<VB : ViewDataBinding> LayoutInflater.createBinding(
    layoutId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false,
    findLifeCycle: Boolean = false
): VB {
    return DataBindingUtil.inflate<VB>(this, layoutId,  container, attachToParent).apply {
        if(findLifeCycle) lifecycleOwner = root.context.getOwner<LifecycleOwner>()
    }
}

/**
 * Create Data binding and return it root view
 *
 * @param layoutId - layout resource id for data binding
 * @param container - parent container
 * @param attachToParent - is need to attach to parent
 */
fun<VB : ViewDataBinding> LayoutInflater.createBindingView(
    layoutId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false): View {
    return createBinding<VB>(layoutId,  container, attachToParent).root
}

fun<VB : ViewDataBinding, VM : ViewModel> Fragment.createBindingWithViewModel(
    layoutId: Int,
    viewModel: VM,
    viewModelBRId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false
): VB {
    return layoutInflater.createBinding<VB>(layoutId, container, attachToParent, false).also {
        it.lifecycleOwner = viewLifecycleOwner
        it.setVariable(viewModelBRId, viewModel)
    }
}

fun<VB : ViewDataBinding, VM : ViewModel> Fragment.createBindingViewWithViewModel(
    layoutId: Int,
    viewModel: VM,
    viewModelBRId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false
): View {
    return layoutInflater.createBinding<VB>(layoutId, container, attachToParent, false).also {
        it.lifecycleOwner = viewLifecycleOwner
        it.setVariable(viewModelBRId, viewModel)
    }.root
}

fun<I : Any, BT : ViewDataBinding> Fragment.createRecyclerConfig(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>.() -> Unit
): DataBindingRecyclerViewConfig<BT> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<I, BT>().apply(block).also { it.lifecycleOwner = viewLifecycleOwner }.build()
}

fun Fragment.createRecyclerMultiConfig(
    block: DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>.() -> Unit
): DataBindingRecyclerViewConfig<ViewDataBinding> {
    return DataBindingRecyclerViewConfig.DataBindingRecyclerViewConfigBuilder<IBindingModel, ViewDataBinding>().apply(block).also { it.lifecycleOwner = viewLifecycleOwner }.build()
}

/**
 * Inline function to retrieve [Context] owners
 */
inline fun <reified T> Context.getOwner(): T {
    var context: Context = this
    while (context !is T) {
        context = (context as ContextWrapper).baseContext
    }
    return context
}