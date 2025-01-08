package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.Player
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@SpringBootTest
class GameWebSocketHandlerIntegrationTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var gameService: GameService

    private lateinit var gameWebSocketHandler: GameWebSocketHandler

    @BeforeEach
    fun setUp() {
        gameWebSocketHandler = GameWebSocketHandler(gameService, objectMapper)
    }


    @Test
    fun `test handleTextMessage JOIN_GAME`() {
        val session = mock(WebSocketSession::class.java)
        val message = IncomeMessage(IncomeAction.JOIN_GAME, JoinGamePayload("Player 1"))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).addPlayer("Player 1", null)
    }

    @Test
    fun `test handleTextMessage SWAP_PLAYERS`() {
        val session = mock(WebSocketSession::class.java)
        val player1Id = "player-1"
        val player2Id = "player-2"
        val message = IncomeMessage(IncomeAction.SWAP_PLAYERS, MovePlayerPayload(player1Id, player2Id))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).swapPlayers(player1Id, player2Id)
    }

    @Test
    fun `test handleTextMessage VIEW_GIFT`() {
        val session = mock(WebSocketSession::class.java)
        val playerId = "player-1"

        val message = IncomeMessage(IncomeAction.VIEW_GIFT, ViewGiftPayload(playerId))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).viewGift(playerId)
    }

    @Test
    fun `test handleTextMessage ROUND_CHANGED`() {
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.START

        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).setCurrentRound(newRound)
    }


    @Test
    fun `test handleTextMessage EXIT_GAME`() {
        val session = mock(WebSocketSession::class.java)
        val playerId = "player-1"
        val player = Player(playerId, "Player 1")
        `when`(gameService.findPlayerById(playerId)).thenReturn(player)

        val message = IncomeMessage(IncomeAction.EXIT_GAME, PlayerExitPayload(playerId))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).findPlayerById(playerId)
        verify(gameService).disconnectPlayer(player)
    }

    @Test
    fun `test handleTextMessage ROUND_CHANGED to SWAP`() {
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.SWAP

        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).setCurrentRound(newRound)
    }

    @Test
    fun `test handleTextMessage ROUND_CHANGED to END`() {
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.END

        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).setCurrentRound(newRound)
    }

    @Test
    fun `test handleTextMessage ROUND_CHANGED to FINAL`() {
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.FINAL

        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        gameWebSocketHandler.handleTextMessage(session, textMessage)

        verify(gameService).setCurrentRound(newRound)
    }
}