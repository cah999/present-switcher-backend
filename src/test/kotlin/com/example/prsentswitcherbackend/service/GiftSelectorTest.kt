package com.example.prsentswitcherbackend.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GiftSelectorTest {

    @BeforeEach
    fun setUp() {
        GiftSelector.resetItems()
    }

    // Классы хороших данных
    @Test
    fun `test readFile when file contains valid data should load all items`() {

        // Arrange
        val filePath = "testdata/valid_items.txt"

        // Act
        GiftSelector.readFile(filePath)

        // Assert
        assertEquals(3, GiftSelector.getItemsCount())
    }

    // Классы плохих данных
    @Test
    fun `test readFile when file contains invalid data should not load any items`() {

        // Arrange
        val filePath = "testdata/invalid_items.txt"

        // Act
        GiftSelector.readFile(filePath)

        // Assert
        assertEquals(0, GiftSelector.getItemsCount())
    }

    // Разделение на классы эквивалентности
    @Test
    fun `test readFile when file contains mixed data should load only valid items`() {

        // Arrange
        val filePath = "testdata/mixed_items.txt"

        // Act
        GiftSelector.readFile(filePath)

        // Assert
        assertEquals(2, GiftSelector.getItemsCount())
    }

    // Неполное тестирование
    @Test
    fun `test findItemsWithSum when no items available should return empty list`() {

        // Arrange
        val sum = 300
        val range = 100
        val itemCount = 2

        // Act
        val result = GiftSelector.findItemsWithSum(itemCount, sum, range)

        // Assert
        assertTrue(result.isEmpty())
    }

    // Классы хороших данных
    @Test
    fun `test findItemsWithSum when items within range on large data should return items with total price in range`() {

        // Arrange
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val sum = 3000
        val range = 1000
        val itemCount = 10

        // Act
        val result = GiftSelector.findItemsWithSum(itemCount, sum, range)

        // Assert
        assertTrue(result.sumOf { it.price } in 2000..4000)
    }

    // Угадывание ошибок
    @Test
    fun `test findItemsWithSum when called multiple times should return different results`() {

        // Arrange
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val sum = 3000
        val range = 1000
        val itemCount = 100

        // Act
        val result1 = GiftSelector.findItemsWithSum(itemCount, sum, range)
        val result2 = GiftSelector.findItemsWithSum(itemCount, sum, range)

        // Assert
        assertNotEquals(result1, result2)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test findItemsWithSum when max iterations reached should return empty or invalid list`() {

        // Arrange
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val sum = 10000
        val range = 1
        val itemCount = 2

        // Act
        val result = GiftSelector.findItemsWithSum(itemCount, sum, range)

        // Assert
        assertTrue(result.isEmpty() || result.sumOf { it.price } !in 9000..11000)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test findItemsWithSum when max iterations not reached should return valid list`() {

        // Arrange
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val sum = 4000
        val range = 1000
        val itemCount = 7

        // Act
        val result = GiftSelector.findItemsWithSum(itemCount, sum, range)

        // Assert
        assertTrue(result.sumOf { it.price } in 3000..5000)
    }

    // Угадывание ошибок
    @Test
    fun `test findItemsWithSum when max iterations reached on large data should return empty or invalid list`() {

        // Arrange
        val filePath = "testdata/large_valid_items.txt"
        GiftSelector.readFile(filePath)
        val sum = 10000
        val range = 10
        val itemCount = 100

        // Act
        val result = GiftSelector.findItemsWithSum(itemCount, sum, range)

        // Assert
        assertTrue(result.isEmpty() || result.sumOf { it.price } !in 9000..11000)
    }
}