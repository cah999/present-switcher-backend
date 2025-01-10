package com.example.prsentswitcherbackend.service

import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

data class Item(val name: String, val price: Int)


object GiftSelector {
    private const val ACCEPTABLE_SUM_RANGE = 1000

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
        val targetRange = (targetSum - ACCEPTABLE_SUM_RANGE)..(targetSum + ACCEPTABLE_SUM_RANGE)
        var result: List<Item>
        var iterations = 0
        do {
            result = items.shuffled().take(itemsCount)
            iterations++
        } while (result.sumOf { it.price } !in targetRange && iterations < maxIterations)
        return result
    }

    // метод для тестов
    fun resetItems() = items.clear()

    fun getItemsCount() = items.size
}