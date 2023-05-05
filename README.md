# easy-recycler-binding
ERB is an library with some useful Kotlin Android data bindings adapters for RecyclerView, ViewPager and ViewPager2 with multiply ViewHolders view

[ ![Kotlin 1.8.21](https://img.shields.io/badge/Kotlin-1.8.21-blue.svg)](http://kotlinlang.org) [![](https://jitpack.io/v/Rasalexman/easy-recycler-binding.svg)](https://jitpack.io/#Rasalexman/easy-recycler-binding)

How to use with RecyclerView:
1) Add to you layout xml file your variable for viewModel and `DataBindingRecyclerViewConfig`
```
<data>
    <variable
        name="vm"
        type="YourViewModelImplementaton" />

        <variable
            name="rvConfig"
            type="com.rasalexman.easyrecyclerbinding.DataBindingRecyclerViewConfig" />
    </data>
```
2) Add data binding properties to your RecyclerView 
`app:items` -  List of ui data models to use in your RecyclerView viewHolders. It can be a multi-layout viewHolders if you implement `com.rasalexman.easyrecyclerbinding.IBindingModel` to your ui data models. 
`app:rv_config="@{rvConfig}"` - your binding config
```
val items: MutableLiveData<MutableList<RecyclerItemUI>> = MutableLiveData()

<androidx.recyclerview.widget.RecyclerView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:items="@{vm.items}"
            app:rv_config="@{rvConfig}"
            />
```
3) After all create binding config from your Fragment and set it to layout variable `rvConfig`. You should defenitely specify `itemId = BR.item` for your viewHolders binding and `layoutId` - for single ViewHolder layout.
```
//in R.layout.item_recycler add
<data>

        <variable
            name="item"
            type="com.rasalexman.erb.models.RecyclerItemUI" />
    </data>

// in Fragment (onViewCreated with binding implementation) add
binding.rvConfig = createRecyclerConfig<RecyclerItemUI, ItemRecyclerBinding> {
	layoutId = R.layout.item_recycler
        itemId = BR.item 
        onItemClick = { item: RecyclerItemUI, position: Int ->
           Log.d("ITEM_POSITION", "Position = $pos")
        }
	onItemCreate = { binding: ItemRecyclerBinding ->
	
	}
	onItemBind = { binding: ItemRecyclerBinding, position: Int ->
	
	}
	onItemDoubleClicked = { item: RecyclerItemUI, position: Int ->
	
	}
	onItemLongClickListener = { item: RecyclerItemUI, position: Int ->
	
	}
        onLoadMore = { position: Int ->
           // load more items
        }
}
```
4) Multi-layout viewHolders can be used with `com.rasalexman.easyrecyclerbinding.IBindingModel`
```
// into your ViewModel
val items: MutableLiveData<MutableList<IBindingModel>> = MutableLiveData()

// into Fragment
binding.rvConfig = createRecyclerMultiConfig {
	itemId = BR.item
}
```

See simple app project for more examples with ViewPager and ViewPager2
	

First of all add repository to your project gradle file
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

App Gradle:
```
// Standart Library
implementation 'com.github.Rasalexman.easy-recycler-binding:easyrecyclerbinding:x.y.z'
```



License
----

MIT License

Copyright (c) 2021 Aleksandr Minkin (sphc@yandex.ru)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

