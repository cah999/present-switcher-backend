package com.example.prsentswitcherbackend.service

import org.springframework.util.ResourceUtils
import java.io.BufferedReader
import java.io.InputStreamReader

data class Item(val name: String, val price: Int)

object ItemProcessor {

    private val items = mutableListOf<Item>()

    fun readFile(filePath: String) {
        val file = ResourceUtils.getFile("classpath:$filePath")
        val reader = BufferedReader(InputStreamReader(file.inputStream()))
        reader.forEachLine { line ->
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
        reader.close()
    }

    fun findItemsWithSum(n: Int, k: Int, maxIterations: Int = 1000): List<Item> {
        val targetRange = (k - 1000)..(k + 1000)
        var result: List<Item>
        var iterations = 0
        do {
            result = items.shuffled().take(n)
            iterations++
        } while (result.sumOf { it.price } !in targetRange && iterations < maxIterations)
        return result
    }
}

fun main() {
    ItemProcessor.readFile("подарки.txt")
    val result = ItemProcessor.findItemsWithSum(9, 10000)
    result.forEach { println(it) }
    val totalSum = result.sumOf { it.price }
    println("Total sum: $totalSum")
}