package com.example.prsentswitcherbackend.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GameServiceTest {

    @Autowired
    private lateinit var gameService: GameService

    @AfterEach
    fun setUp() {
        gameService.endGame()
    }

    // Классы хороших данных
    @Test
    fun `test addPlayer when player is new should return created player`() {
        // Arrange
        val playerName = "Player1"

        // Act
        val player = gameService.addPlayer(playerName, null)

        // Assert
        assertNotNull(player)
        assertEquals(playerName, player?.name)
    }

    // Классы плохих данных
    @Test
    fun `test addPlayer when player already exists should return null`() {
        // Arrange
        val playerName = "Player1"
        gameService.addPlayer(playerName, null)

        // Act
        val duplicatePlayer = gameService.addPlayer(playerName, null)

        // Assert
        assertNull(duplicatePlayer)
    }

    // Анализ граничных условий
    @Test
    fun `test addPlayer when maximum players reached should return null`() {
        // Arrange
        repeat(9) { gameService.addPlayer("Player$it", null) }

        // Act
        val extraPlayer = gameService.addPlayer("Player10", null)

        // Assert
        assertNull(extraPlayer)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test findAndSetNextPlayerTurn when called should return next player`() {
        // Arrange
        val player1 = gameService.addPlayer("Player1", null)
        val player2 = gameService.addPlayer("Player2", null)
        player1?.turn = 0
        player2?.turn = 1

        // Act
        val nextPlayer = gameService.findAndSetNextPlayerTurn(player1!!.id)

        // Assert
        assertEquals(player2, nextPlayer)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test findAndSetFirstPlayerTurn when called should return first player`() {
        // Arrange
        val player1 = gameService.addPlayer("Player1", null)
        gameService.addPlayer("Player2", null)
        player1?.turn = 0

        // Act
        val firstPlayer = gameService.findAndSetFirstPlayerTurn()

        // Assert
        assertEquals(player1, firstPlayer)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test resetCurrentTurnPlayer when called should reset current turn player`() {
        // Arrange
        gameService.addPlayer("Player1", null)
        gameService.getAllPlayersShuffled()
        gameService.findAndSetFirstPlayerTurn()

        // Act
        gameService.resetCurrentTurnPlayer()

        // Assert
        assertNull(gameService.getCurrentTurnPlayer())
    }

    // Классы хороших данных
    @Test
    fun `test getAllPlayers when called should return list of players`() {
        // Arrange
        gameService.addPlayer("Player1", null)
        gameService.addPlayer("Player2", null)

        // Act
        val players = gameService.getAllPlayers()

        // Assert
        assertEquals(2, players.size)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test swapPlayers when called should swap positions of two players`() {
        // Arrange
        val player1 = gameService.addPlayer("Player1", null)
        val player2 = gameService.addPlayer("Player2", null)
        player1?.position = 0
        player2?.position = 1

        // Act
        gameService.swapPlayers(player1!!.id, player2!!.id)

        // Assert
        assertEquals(1, player1.position)
        assertEquals(0, player2.position)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test viewGift when called should return gift for player`() {
        // Arrange
        val player = gameService.addPlayer("Player1", null)
        gameService.initializeItems()
        gameService.getAllPlayersShuffled()

        // Act
        val gift = gameService.viewGift(player!!.id)

        // Assert
        assertNotNull(gift)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test disconnectPlayer when called should mark player as disconnected`() {
        // Arrange
        val player = gameService.addPlayer("Player1", null)

        // Act
        gameService.disconnectPlayer(player!!)

        // Assert
        assertTrue(player.isDisconnected)
    }
}