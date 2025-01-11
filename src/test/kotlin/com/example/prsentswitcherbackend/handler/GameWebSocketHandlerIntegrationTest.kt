package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.command.GameActionCommandFactory
import com.example.prsentswitcherbackend.model.Player
import com.example.prsentswitcherbackend.model.enums.IncomeAction
import com.example.prsentswitcherbackend.model.enums.ROUND
import com.example.prsentswitcherbackend.model.income.*
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.strategy.RoundProcessorFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
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

    private lateinit var gameWebSocketHandler: TestGameWebSocketHandler

    @BeforeEach
    fun setUp() {
        val messageService = MessageService()
        gameWebSocketHandler = TestGameWebSocketHandler(
            messageService,
            GameActionCommandFactory(messageService, gameService, RoundProcessorFactory(gameService, messageService))
        )
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test handleTextMessage when action is JOIN_GAME should add player`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val message = IncomeMessage(IncomeAction.JOIN_GAME, JoinGamePayload("Player 1"))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).addPlayer("Player 1", null)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test handleTextMessage when action is SWAP_PLAYERS should swap players`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val player1Id = "player-1"
        val player2Id = "player-2"
        val message = IncomeMessage(IncomeAction.SWAP_PLAYERS, MovePlayerPayload(player1Id, player2Id))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).swapPlayers(player1Id, player2Id)
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test handleTextMessage when action is VIEW_GIFT should view gift for player`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val playerId = "player-1"
        val message = IncomeMessage(IncomeAction.VIEW_GIFT, ViewGiftPayload(playerId))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).viewGift(playerId)
    }

    // Угадывание ошибок
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to START should set round and initialize items`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.START
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
        verify(gameService).initializeItems()
    }

    // Тестирование, основанное на потоках данных
    @Test
    fun `test handleTextMessage when action is EXIT_GAME should disconnect player`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val playerId = "player-1"
        val player = Player(playerId, "Player 1")
        `when`(gameService.findPlayerById(playerId)).thenReturn(player)
        val message = IncomeMessage(IncomeAction.EXIT_GAME, PlayerExitPayload(playerId))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).findPlayerById(playerId)
        verify(gameService).disconnectPlayer(player)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to SWAP should set round to SWAP`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.SWAP
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to END should set round to END`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.END
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to FINAL should set round to FINAL`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.FINAL
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to WAITING should set round to WAITING and end game`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.WAITING
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
        verify(gameService).endGame()
    }

    // Структурированное базисное тестирование
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to TALK should set round`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.TALK
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
    }

    // Структурированное базисное тестирование
    @Test
    fun `test handleTextMessage when action is ROUND_CHANGED to START should set round and shuffle players`() {
        // Arrange
        val session = mock(WebSocketSession::class.java)
        val newRound = ROUND.START
        val message = IncomeMessage(IncomeAction.ROUND_CHANGED, RoundChangePayload(newRound))
        val textMessage = TextMessage(objectMapper.writeValueAsString(message))

        // Act
        gameWebSocketHandler.handleTextMessage(session, textMessage)

        // Assert
        verify(gameService).setCurrentRound(newRound)
        verify(gameService).getAllPlayersShuffled()
    }
}
