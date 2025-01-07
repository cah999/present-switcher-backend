package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.Player
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArrayList

@Controller
class GameWebSocketHandler(
    private val gameService: GameService,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    private val sessions = CopyOnWriteArrayList<WebSocketSession>()
    private val logger: Logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java)

    private fun sendPlayers() {
        val players = gameService.getAllPlayers()
        sessions.forEach {
            it.sendMessage(
                TextMessage(
                    objectMapper.writeValueAsString(
                        OutcomeMessage(
                            OutcomeAction.UPDATE_PLAYERS,
                            players
                        )
                    )
                )
            )
        }
    }

    fun broadcastPlayersSwap(player1Id: String, player2Id: String) {
        val message = OutcomeMessage(OutcomeAction.UPDATE_SWAPPED_PLAYERS, Pair(player1Id, player2Id))
        val serializedMessage = objectMapper.writeValueAsString(message)
        sessions.forEach { it.sendMessage(TextMessage(serializedMessage)) }
    }

    private fun send(session: WebSocketSession, message: OutcomeMessage<*>) {
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }

    private fun sendAllPlayersStartPositions() {
        val players = gameService.getAllPlayersShuffled()
        val message = OutcomeMessage(OutcomeAction.START_QUEUE, StartQueueOutcomeData(players))
        val serializedMessage = objectMapper.writeValueAsString(message)
        sessions.forEach { it.sendMessage(TextMessage(serializedMessage)) }
    }

    private fun sendAllPlayersFinalQueue() {
        val players = gameService.getAllPlayersShuffledQueue()
        val message = OutcomeMessage(OutcomeAction.FINAL_QUEUE, FinalQueueOutcomeData(players))
        val serializedMessage = objectMapper.writeValueAsString(message)
        sessions.forEach { it.sendMessage(TextMessage(serializedMessage)) }
    }

    // обновить раунд игры
    fun changeRound(round: ROUND) {
        if (gameService.getCurrentRound() != ROUND.SWAP) {
            gameService.resetCurrentTurnPlayer()
            val turnMessage = OutcomeMessage(OutcomeAction.PLAYER_TURN, null)
            val serializedTurnMessage = objectMapper.writeValueAsString(turnMessage)
            sessions.forEach { it.sendMessage(TextMessage(serializedTurnMessage)) }
        }
        gameService.setCurrentRound(round)
        val roundMessage = OutcomeMessage(OutcomeAction.ROUND_NAME, round.value)
        val serializedRoundMessage = objectMapper.writeValueAsString(roundMessage)
        sessions.forEach { it.sendMessage(TextMessage(serializedRoundMessage)) }
    }

    fun sendStartData(session: WebSocketSession, player: Player) {
        send(session, OutcomeMessage(OutcomeAction.ROUND_NAME, gameService.getCurrentRound().value))
        send(session, OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, gameService.getAllPlayers()))
        if (gameService.getCurrentRound() == ROUND.END) {
            send(session, OutcomeMessage(OutcomeAction.GAME_FINISH, gameService.getAllGifts()))
        }
        send(session, OutcomeMessage(OutcomeAction.PLAYER_TURN, gameService.getCurrentTurnPlayer()))
        send(session, OutcomeMessage(OutcomeAction.JOINED_PLAYER, player))
    }

    private fun sendEndGame() {
        val players = gameService.getAllPlayers()
        val updateMessage = OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, players)
        sessions.forEach { it.sendMessage(TextMessage(objectMapper.writeValueAsString(updateMessage))) }

        val round = gameService.getCurrentRound()
        val roundMessage = OutcomeMessage(OutcomeAction.ROUND_NAME, round.value)
        sessions.forEach { it.sendMessage(TextMessage(objectMapper.writeValueAsString(roundMessage))) }

        val currentTurnPlayer = gameService.getCurrentTurnPlayer()
        val turnMessage = OutcomeMessage(OutcomeAction.PLAYER_TURN, currentTurnPlayer)
        sessions.forEach { it.sendMessage(TextMessage(objectMapper.writeValueAsString(turnMessage))) }
    }

    fun sendGameFinal() {
        val gifts = gameService.getAllGifts()
        val message = OutcomeMessage(OutcomeAction.GAME_FINISH, gifts)
        sessions.forEach { it.sendMessage(TextMessage(objectMapper.writeValueAsString(message))) }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("New connection established: $session")
        sessions.add(session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Error handling transport in session: $session, exc ${exception.message}", exception)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            logger.info("Received message: ${message.payload}")
            val incomingMessage = objectMapper.readValue(message.payload, IncomeMessage::class.java)
            logger.info("Received message: $incomingMessage")

            when (incomingMessage.action) {
                IncomeAction.JOIN_GAME -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, JoinGamePayload::class.java)
                    val player = gameService.addPlayer(payload.name, payload.playerId)
                    println("Joined player $player")
                    if (player != null) {
                        sendStartData(session, player)
                        sendPlayers()
                        return
                    }
                }

                IncomeAction.SWAP_PLAYERS -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, MovePlayerPayload::class.java)
                    gameService.swapPlayers(payload.player1Id, payload.player2Id)
                    broadcastPlayersSwap(payload.player1Id, payload.player2Id)

                    val turnMessage = OutcomeMessage(
                        OutcomeAction.PLAYER_TURN,
                        gameService.findNextPlayerTurn(currentPlayerIdTurn = payload.player1Id) ?: ""
                    )
                    val serializedTurnMessage = objectMapper.writeValueAsString(turnMessage)
                    sessions.forEach { it.sendMessage(TextMessage(serializedTurnMessage)) }
                }

                IncomeAction.VIEW_GIFT -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, ViewGiftPayload::class.java)
                    val giftContent = gameService.viewGift(payload.playerId)
                    println("giftContent: $giftContent")
                    send(session, OutcomeMessage(OutcomeAction.VIEW_GIFT, giftContent))
                }

                IncomeAction.ROUND_CHANGED -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, RoundChangePayload::class.java)
                    changeRound(payload.newRound)
                    when (payload.newRound) {
                        ROUND.WAITING -> {
                            gameService.endGame()
                            sendEndGame()
                        }

                        ROUND.START -> {
                            gameService.initializeItems()
                            sendAllPlayersStartPositions()
                        }

                        ROUND.TALK -> {
                            gameService.setPlayerTurns()
                            sendPlayers()
                        }

                        ROUND.SWAP -> {
                            val turnMessage = OutcomeMessage(
                                OutcomeAction.PLAYER_TURN,
                                gameService.findFirstPlayerTurn()
                            )
                            val serializedTurnMessage = objectMapper.writeValueAsString(turnMessage)
                            sessions.forEach { it.sendMessage(TextMessage(serializedTurnMessage)) }
                        }

                        ROUND.FINAL -> {
                            sendAllPlayersFinalQueue()
                        }

                        ROUND.END -> {
                            sendGameFinal()
                        }
                    }
                }

                IncomeAction.EXIT_GAME -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, PlayerExitPayload::class.java)
                    val player = gameService.findPlayerById(payload.playerId)
                    if (player != null) {
                        gameService.disconnectPlayer(player)
                        sendPlayers()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error handling message", e)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        logger.info("Connection closed: $session")
        sessions.remove(session)
    }
}

// убрать потом )))
data class IncomeMessage<T>(
    val action: IncomeAction,
    val data: T
)

data class OutcomeMessage<T>(
    val action: OutcomeAction,
    val data: T
)

data class JoinGamePayload(val name: String, val playerId: String? = null)
data class MovePlayerPayload(val player1Id: String, val player2Id: String)
data class ViewGiftPayload(val playerId: String)
data class RoundChangePayload(val newRound: ROUND)

enum class IncomeAction {
    JOIN_GAME,
    EXIT_GAME,
    SWAP_PLAYERS,
    VIEW_GIFT,
    ROUND_CHANGED,
}

enum class OutcomeAction {
    JOINED_PLAYER,
    UPDATE_PLAYERS,
    UPDATE_SWAPPED_PLAYERS,
    VIEW_GIFT,
    ROUND_NAME,
    START_QUEUE,
    FINAL_QUEUE,
    PLAYER_TURN,
    GAME_FINISH
}

data class StartQueueOutcomeData(val queue: List<Player>)
data class FinalQueueOutcomeData(val queue: List<Player>)
data class PlayerExitPayload(val playerId: String)


enum class ROUND(val value: String) {
    WAITING("ожидание участников"),
    START("распределение подарков"),
    TALK("обсуждение"),
    SWAP("обмен подарками"),
    FINAL("очередь на финал"),
    END("итоги"),
}