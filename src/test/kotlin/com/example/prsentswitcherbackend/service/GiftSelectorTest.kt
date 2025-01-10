package com.example.prsentswitcherbackend.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GiftSelectorTest {

    @BeforeEach
    fun setUp() {
        GiftSelector.resetItems()
    }

    @Test
    fun `test readFile with valid data`() {
        val filePath = "testdata/valid_items.txt"
        GiftSelector.readFile(filePath)
        assertEquals(3, GiftSelector.getItemsCount())
    }

    @Test
    fun `test readFile with invalid data`() {
        val filePath = "testdata/invalid_items.txt"
        GiftSelector.readFile(filePath)
        assertEquals(0, GiftSelector.getItemsCount())
    }

    @Test
    fun `test readFile with mixed data`() {
        val filePath = "testdata/mixed_items.txt"
        GiftSelector.readFile(filePath)
        assertEquals(2, GiftSelector.getItemsCount())
    }


    @Test
    fun `test findItemsWithSum no items`() {
        val result = GiftSelector.findItemsWithSum(2, 300, 100)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test findItemsWithSum within range on large data`() {
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val result = GiftSelector.findItemsWithSum(10, 3000, 1000)
        assertTrue(result.sumOf { it.price } in 2000..4000)
    }

    @Test
    fun `test findItemsWithSum results are different each time`() {
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val result1 = GiftSelector.findItemsWithSum(100, 3000, 1000)
        val result2 = GiftSelector.findItemsWithSum(100, 3000, 1000)
        assertNotEquals(result1, result2)
    }

    @Test
    fun `test findItemsWithSum max iterations reached`() {
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val result = GiftSelector.findItemsWithSum(2, 10000, 1)
        assertTrue(result.isEmpty() || result.sumOf { it.price } !in 9000..11000)
    }

    @Test
    fun `test findItemsWithSum max iterations not reached`() {
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val result = GiftSelector.findItemsWithSum(7, 4000, 1000)
        assertTrue(result.sumOf { it.price } in 3000..5000)
    }

    @Test
    fun `test findItemsWithSum max iterations with large data`() {
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val result = GiftSelector.findItemsWithSum(100, 10000, 10)
        assertTrue(result.isEmpty() || result.sumOf { it.price } !in 9000..11000)
    }
}