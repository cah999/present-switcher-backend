package com.example.prsentswitcherbackend.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ItemProcessorTest {

    @BeforeEach
    fun setUp() {
        ItemProcessor.resetItems()
    }

    @Test
    fun `test readFile with valid data`() {
        val filePath = "testdata/valid_items.txt"
        ItemProcessor.readFile(filePath)
        assertEquals(3, ItemProcessor.getItemsCount())
    }

    @Test
    fun `test readFile with invalid data`() {
        val filePath = "testdata/invalid_items.txt"
        ItemProcessor.readFile(filePath)
        assertEquals(0, ItemProcessor.getItemsCount())
    }

    @Test
    fun `test readFile with mixed data`() {
        val filePath = "testdata/mixed_items.txt"
        ItemProcessor.readFile(filePath)
        assertEquals(2, ItemProcessor.getItemsCount())
    }


    @Test
    fun `test findItemsWithSum no items`() {
        val result = ItemProcessor.findItemsWithSum(2, 300, 100)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test findItemsWithSum within range on large data`() {
        val filePath = "testdata/large_valid_items.txt"
        ItemProcessor.readFile(filePath)
        val result = ItemProcessor.findItemsWithSum(10, 3000, 1000)
        assertTrue(result.sumOf { it.price } in 2000..4000)
    }

    @Test
    fun `test findItemsWithSum results are different each time`() {
        val filePath = "testdata/large_valid_items.txt"
        ItemProcessor.readFile(filePath)
        val result1 = ItemProcessor.findItemsWithSum(100, 3000, 1000)
        val result2 = ItemProcessor.findItemsWithSum(100, 3000, 1000)
        assertNotEquals(result1, result2)
    }

    @Test
    fun `test findItemsWithSum max iterations reached`() {
        val filePath = "testdata/large_valid_items.txt"
        ItemProcessor.readFile(filePath)
        val result = ItemProcessor.findItemsWithSum(2, 10000, 1)
        println(result)
        println(result.sumOf { it.price })
        assertTrue(result.isEmpty() || result.sumOf { it.price } !in 9000..11000)
    }

    @Test
    fun `test findItemsWithSum max iterations not reached`() {
        val filePath = "testdata/large_valid_items.txt"
        ItemProcessor.readFile(filePath)
        val result = ItemProcessor.findItemsWithSum(7, 4000, 1000)
        println(result)
        println(result.sumOf { it.price })
        assertTrue(result.sumOf { it.price } in 3000..5000)
    }

    @Test
    fun `test findItemsWithSum max iterations with large data`() {
        val filePath = "testdata/large_valid_items.txt"
        ItemProcessor.readFile(filePath)
        val result = ItemProcessor.findItemsWithSum(100, 10000, 10)
        assertTrue(result.isEmpty() || result.sumOf { it.price } !in 9000..11000)
    }
}