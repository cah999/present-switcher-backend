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

    @Test
    fun `test addPlayer with new player`() {
        val player = gameService.addPlayer("Player1", null)
        assertNotNull(player)
        assertEquals("Player1", player?.name)
    }

    @Test
    fun `test addPlayer with existing player`() {
        val player1 = gameService.addPlayer("Player1", null)
        val player2 = gameService.addPlayer("Player1", null)
        assertNotNull(player1)
        assertNull(player2)
    }

    @Test
    fun `test addPlayer with maximum players`() {
        repeat(9) { gameService.addPlayer("Player$it", null) }
        val player = gameService.addPlayer("Player10", null)
        assertNull(player)
    }

    @Test
    fun `test findNextPlayerTurn`() {
        val player1 = gameService.addPlayer("Player1", null)
        val player2 = gameService.addPlayer("Player2", null)
        player1?.turn = 0
        player2?.turn = 1
        val nextPlayer = gameService.findNextPlayerTurn(player1!!.id)
        assertEquals(player2, nextPlayer)
    }

    @Test
    fun `test findFirstPlayerTurn`() {
        val player1 = gameService.addPlayer("Player1", null)
        val player2 = gameService.addPlayer("Player2", null)
        player1?.turn = 0
        player2?.turn = 1
        val firstPlayer = gameService.findFirstPlayerTurn()
        assertEquals(player1, firstPlayer)
    }

    @Test
    fun `test resetCurrentTurnPlayer`() {
        gameService.addPlayer("Player1", null)
        gameService.getAllPlayersShuffled()
        val turn = gameService.findFirstPlayerTurn()
        gameService.resetCurrentTurnPlayer()
        assertNotNull(turn)
        assertNull(gameService.getCurrentTurnPlayer())
    }

    @Test
    fun `test getAllPlayers`() {
        gameService.addPlayer("Player1", null)
        gameService.addPlayer("Player2", null)
        val players = gameService.getAllPlayers()
        assertEquals(2, players.size)
    }

    @Test
    fun `test swapPlayers`() {
        val player1 = gameService.addPlayer("Player1", null)
        val player2 = gameService.addPlayer("Player2", null)
        player1?.position = 0
        player2?.position = 1
        gameService.swapPlayers(player1!!.id, player2!!.id)
        assertEquals(1, player1.position)
        assertEquals(0, player2.position)
    }

    @Test
    fun `test viewGift`() {
        val player = gameService.addPlayer("Player1", null)
        gameService.initializeItems()
        gameService.getAllPlayersShuffled()
        val gift = gameService.viewGift(player!!.id)
        assertNotNull(gift)
    }

    @Test
    fun `test disconnectPlayer`() {
        val player = gameService.addPlayer("Player1", null)
        gameService.disconnectPlayer(player!!)
        assertTrue(player.isDisconnected)
    }
}