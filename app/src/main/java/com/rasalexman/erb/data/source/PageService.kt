package com.rasalexman.erb.data.source

import com.rasalexman.erb.models.PageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

class PageService : IPageService {
    private val allData = mutableMapOf<Int, List<PageModel>>()

    override suspend fun loadItems(position: Int, loadSize: Int): List<PageModel> = withContext(Dispatchers.Default) {
        allData.getOrPut(position) {
            delay(2000L)
            createItems(position, loadSize)
        }
    }

    private fun createItems(position: Int, loadSize: Int): List<PageModel> {
        val itemsList = mutableListOf<PageModel>()
        val randomSize = Random.nextInt(1, loadSize)
        repeat(randomSize) {
            val randomId = UUID.randomUUID().toString()
            val randomTitle = UUID.randomUUID().toString()
            itemsList.add(
                PageModel(
                    id = randomId.take(Random.nextInt(15, randomId.length)),
                    title = randomTitle.take(Random.nextInt(20, randomTitle.length))
                )
            )
        }
        println("------> Finish Loading at position = $position | size = ${itemsList.size}")
        return itemsList
    }
}

interface IPageService {
    suspend fun loadItems(position: Int = 0, loadSize: Int = 500): List<PageModel>
}