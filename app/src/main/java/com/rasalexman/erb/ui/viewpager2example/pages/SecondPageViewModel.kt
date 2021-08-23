package com.rasalexman.erb.ui.viewpager2example.pages

import androidx.lifecycle.*
import com.rasalexman.easyrecyclerbinding.DiffCallback
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.recyclerMultiConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.SimpleRecyclerItemUI
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import com.rasalexman.erb.ui.viewpager2example.SearchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.*
import kotlin.random.Random

class SecondPageViewModel : BaseItemsViewModel(), IBindingModel {
    override val layoutResId: Int
        get() = R.layout.item_vp2_second_page

    private val searchQuery: MutableLiveData<String> = MutableLiveData<String>("")

    val searchState = MutableLiveData(SearchState.CLOSED)

    @FlowPreview
    val currentItems: LiveData<List<IRecyclerItem>> = items.switchMap { list ->
        liveData(Dispatchers.Default) {
            searchQuery.asFlow().debounce(200L).distinctUntilChanged().collect { query ->
                val currentList = if (query.isEmpty()) {
                    list
                } else {
                    list.filter { it.title.contains(query, true) }
                }
                emit(currentList)
            }
        }
    }

    fun onQueryTextChanged(newText: String) {
        searchQuery.postValue(newText)
    }

    fun createRvConfig() = recyclerMultiConfig {
        itemId = BR.item
        layoutId = R.layout.item_recycler

        onItemClick = { item, _ ->
            item as IRecyclerItem
            navigationState.value =
                MainFragmentDirections.showSelectedFragment(selectedItem = item.title)
        }

        //isLifecyclePending = true

        diffUtilCallback = object : DiffCallback<IRecyclerItem>() {
            override fun areItemsTheSame(
                oldItem: IRecyclerItem,
                newItem: IRecyclerItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
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