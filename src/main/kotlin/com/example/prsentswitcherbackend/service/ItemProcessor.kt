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
//        file.forEachLine { line ->
//            val parts = line.split("-")
//            if (parts.size == 2) {
//                val name = parts[0].trim()
//                val price = parts[1].trim().toIntOrNull()
//                if (price != null) {
//                    items.add(Item(name, price))
//                }
//            }
//        }
        println("Read ${items.size} items")
        println(items)
    }

    fun findItemsWithSum(n: Int, k: Int, maxIterations: Int): List<Item> {
        val targetRange = (k - 1000)..(k + 1000)
        var result: List<Item>
        var iterations = 0
        do {
            result = items.shuffled().take(n)
            iterations++
        } while (result.sumOf { it.price } !in targetRange && iterations < maxIterations)
        return result
    }

    fun resetItems() = items.clear()


    fun getItemsCount() = items.size
}

fun main() {
    var gifts: List<Gift> = emptyList()
    ItemProcessor.readFile("presents.txt")
//    println("Старт игры для ${players.size} игроков")
    val items = ItemProcessor.findItemsWithSum(8, 10000, 500)
    val totalSum = items.sumOf { it.price }
    println("total sum: $totalSum")
    println("Items $items")
    gifts = items.mapIndexed { index, item -> Gift(index, item.name) }
    println(gifts)
//    println("Total sum: $totalSum")
//    gifts = items.mapIndexed { index, item -> Gift(index, item.name) }
//    println("Gifts: $gifts")
}