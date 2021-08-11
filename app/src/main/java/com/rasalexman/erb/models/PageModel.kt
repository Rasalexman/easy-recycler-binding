package com.rasalexman.erb.models

data class PageModel(
    val id: String?,
    val title: String?
) {
    fun convert(): IRecyclerItem {
        return RecyclerItemUI(
            id = id.orEmpty(),
            title = title.orEmpty()
        )
    }
}
