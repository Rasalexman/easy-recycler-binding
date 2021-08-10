package com.rasalexman.erb.models

import androidx.databinding.ObservableBoolean
import com.rasalexman.easyrecyclerbinding.IBindingModel

interface IRecyclerItem : IBindingModel {
    val id: String
    val title: String
    val isChecked: ObservableBoolean
}