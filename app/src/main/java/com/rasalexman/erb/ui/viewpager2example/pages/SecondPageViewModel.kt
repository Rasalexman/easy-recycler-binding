package com.rasalexman.erb.ui.viewpager2example.pages

import androidx.lifecycle.*
import com.rasalexman.easyrecyclerbinding.DiffCallback
import com.rasalexman.easyrecyclerbinding.IBindingModel
import com.rasalexman.easyrecyclerbinding.recyclerConfig
import com.rasalexman.erb.BR
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.ItemRecyclerBinding
import com.rasalexman.erb.models.IRecyclerItem
import com.rasalexman.erb.models.SimpleRecyclerItemUI
import com.rasalexman.erb.ui.base.BaseItemsViewModel
import com.rasalexman.erb.ui.main.MainFragmentDirections
import com.rasalexman.erb.ui.viewpager2example.SearchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.*
import kotlin.random.Random

class SecondPageViewModel : BaseItemsViewModel(), IBindingModel {
    override val layoutResId: Int
        get() = R.layout.item_vp2_second_page

    private val searchQuery: MutableLiveData<String> = MutableLiveData<String>("")

    val searchState = MutableLiveData<SearchState>(SearchState.CLOSED)

    val currentItems: LiveData<List<IRecyclerItem>> = items.switchMap { list ->
        liveData(Dispatchers.Default) {
            searchQuery.asFlow().debounce(200L).distinctUntilChanged().collect { query ->
                val currentList = if (query.isEmpty()) {
                    list
                } else {
                    list.filter { it.title.contains(query) }
                }
                emit(currentList)
            }
        }
    }

    fun onQueryTextChanged(newText: String) {
        searchQuery.postValue(newText)
    }

    fun createRvConfig() = recyclerConfig<SimpleRecyclerItemUI, ItemRecyclerBinding> {
        itemId = BR.item
        layoutId = R.layout.item_recycler

        onItemClick = { item, _ ->
            navigationState.value =
                MainFragmentDirections.showSelectedFragment(selectedItem = item.title)
        }

        diffUtilCallback = object : DiffCallback<SimpleRecyclerItemUI>() {
            override fun areItemsTheSame(
                oldItem: SimpleRecyclerItemUI?,
                newItem: SimpleRecyclerItemUI?
            ): Boolean {
                return if (oldItem != null && newItem != null) {
                    oldItem.id == newItem.id
                } else {
                    oldItem == null && newItem == null
                }
            }
        }
    }

    fun onClearButtonClicked() {
        clearItems()
        searchQuery.postValue("")
        searchState.postValue(SearchState.CLOSED)
    }

    fun onGenerateButtonClicked() {
        val minItems = Random.nextInt(100, 500)
        val maxItems = Random.nextInt(500, 10000)
        createItems(minItems, maxItems)
    }

    fun onAddButtonClicked() {
        val minItems = Random.nextInt(100, 500)
        val maxItems = Random.nextInt(500, 10000)
        addItems(minItems, maxItems)
    }

    override suspend fun itemsCreator(position: Int): IRecyclerItem {
        return SimpleRecyclerItemUI(
            title = UUID.randomUUID().toString().take(14),
            id = UUID.randomUUID().toString()
        )
    }
}