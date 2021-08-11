package com.rasalexman.easyrecyclerbinding

import androidx.paging.LoadState

interface ILoadStateModel : IBindingModel {
    var loadState: LoadState
}