package com.example.prsentswitcherbackend.service

import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

data class Item(val name: String, val price: Int)

object ItemProcessor {

    private val items = mutableListOf<Item>()

    fun readFile(filePath: String) {
        val file = StreamUtils.copyToString(ClassPathResource(filePath).inputStream, StandardCharsets.UTF_8)
        file.lines().forEach { line ->
            val parts = line.split("-")
            if (parts.size == 2) {
                val name = parts[0].trim()
                val price = parts[1].trim().toIntOrNull()
                if (price != null) {
                    items.add(Item(name, price))
                }
            }
        }
        println("Read ${items.size} items")
        println(items)
    }

    fun findItemsWithSum(itemsCount: Int, targetSum: Int, maxIterations: Int): List<Item> {
        val targetRange = (targetSum - 1000)..(targetSum + 1000)
        var result: List<Item>
        var iterations = 0
        do {
            result = items.shuffled().take(itemsCount)
            iterations++
        } while (result.sumOf { it.price } !in targetRange && iterations < maxIterations)
        return result
    }

    // методы для тестов
    fun resetItems() = items.clear()


    fun getItemsCount() = items.size
}

fun main() {
    var gifts: List<Gift> = emptyList()
    ItemProcessor.readFile("presents.txt")
    val items = ItemProcessor.findItemsWithSum(8, 10000, 500)
    val totalSum = items.sumOf { it.price }
    println("total sum: $totalSum")
    println("Items $items")
    gifts = items.mapIndexed { index, item -> Gift(index, item.name) }
    println(gifts)
}