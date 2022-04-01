package com.rasalexman.erb.ui.viewpager2example.pages

import androidx.lifecycle.*
import com.rasalexman.easyrecyclerbinding.DiffCallback
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.recyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.common.StringUtils
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.SimpleRecyclerItemUI
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.viewpager2example.SearchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random

class SecondPageViewModel : BaseItemsViewModel(), IBindingModel {

    private val diffCallback by lazy {
        object : DiffCallback<SimpleRecyclerItemUI>() {
            override fun areItemsTheSame(
                oldItem: SimpleRecyclerItemUI,
                newItem: SimpleRecyclerItemUI
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SimpleRecyclerItemUI,
                newItem: SimpleRecyclerItemUI
            ): Boolean {
                return oldItem.descriptionText == newItem.descriptionText
            }
        }
    }

    override val layoutResId: Int
        get() = R.layout.item_vp2_second_page

    private val searchQuery: MutableLiveData<String> = MutableLiveData<String>("")

    val searchState = MutableLiveData(SearchState.CLOSED)

    val currentQuery: String
        get() = searchQuery.value.orEmpty()

    val foundItems: LiveData<List<IRecyclerItem>> = items.switchMap { list ->
        searchQuery.distinctUntilChanged().switchMap { query ->
            liveData(Dispatchers.Default) {
                showLoading()
                delay(300L)
                val currentList = if (query.isEmpty()) {
                    list
                } else {
                    list.filter { it.title.contains(query, true) }.mapNotNull {
                        (it as? SimpleRecyclerItemUI)?.copy()?.apply {
                            descriptionText = StringUtils.getFilterSearchColoredText(query, title, false)
                        }
                    }
                }
                emit(currentList)
            }
        }
    }

    override val itemsCount: LiveData<String> by lazy {
        foundItems.map {
            hideLoading()
            "Items count: ${it.size}"
        }
    }

    fun onQueryTextChanged(newText: String) {
        searchQuery.postValue(newText)
    }

    fun createRvConfig() = recyclerMultiConfig {
        itemId = BR.item
        layoutId = R.layout.item_simple_recycler
        diffUtilCallback = diffCallback

        onItemPosClick = { item, _ ->
            onShowSelectedBindingItemFragment(item)
        }

        onModelBind = { model, pos ->
            println("----> Model binded on pos = $pos with id = ${model::class.simpleName}")
        }
    }

    fun onClearButtonClicked() {
        clearItems()
        searchQuery.postValue("")
        searchState.postValue(SearchState.CLOSED)
    }

    fun onGenerateButtonClicked() {
        val minItems = Random.nextInt(10, 100)
        val maxItems = Random.nextInt(minItems+10, 200)
        createItems(minItems, maxItems)
    }

    fun onAddButtonClicked() {
        val minItems = Random.nextInt(100, 500)
        val maxItems = Random.nextInt(500, 10000)
        val isFirst = minItems%2 == 0
        addItems(minItems, maxItems, isFirst)
    }

    override suspend fun itemsCreator(position: Int): IRecyclerItem {
        return SimpleRecyclerItemUI(
            title = UUID.randomUUID().toString().take(14),
            id = UUID.randomUUID().toString()
        )
    }
}