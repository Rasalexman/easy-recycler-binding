package com.rasalexman.erb.ui.selected

import androidx.lifecycle.SavedStateHandle
import com.rasalexman.erb.ui.base.BaseViewModel

class SelectedViewModel(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val selectedItem = savedStateHandle.getLiveData<String>("selectedItem")
}