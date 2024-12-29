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
                        broadcastPlayers()
                        return
                    }
                }

                IncomeAction.SWAP_PLAYERS -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, MovePlayerPayload::class.java)
                    gameService.swapPlayers(payload.player1Id, payload.player2Id)
                    broadcastPlayersSwap(payload.player1Id, payload.player2Id)
                    broadcast(
                        OutcomeMessage(
                            OutcomeAction.PLAYER_TURN,
                            gameService.findNextPlayerTurn(currentPlayerIdTurn = payload.player1Id) ?: ""
                        )
                    )
                }

                IncomeAction.VIEW_GIFT -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, ViewGiftPayload::class.java)
                    val giftContent = gameService.viewGift(payload.playerId)
                    println("giftContent: $giftContent")
                    sendToPlayer(session, OutcomeMessage(OutcomeAction.VIEW_GIFT, giftContent))
                }

                IncomeAction.ROUND_CHANGED -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, RoundChangePayload::class.java)
                    changeRound(payload.newRound)
                    when (payload.newRound) {
                        ROUND.WAITING -> {
                            gameService.endGame()
                            sendAllPlayersEndGame()
                        }

                        ROUND.START -> {
                            gameService.initializeItems()
                            sendAllPlayersStartPositions()
                        }

                        ROUND.TALK -> {
                            gameService.setPlayerTurns()
                            broadcastPlayers()
                        }

                        ROUND.SWAP -> {
                            broadcast(
                                OutcomeMessage(
                                    OutcomeAction.PLAYER_TURN,
                                    gameService.findFirstPlayerTurn()
                                )
                            )
                        }

                        ROUND.FINAL -> {
                            sendAllPlayersFinalQueue()
                        }


                        ROUND.END -> {
                            broadcastGameFinal()
                        }
                    }
                }

                IncomeAction.EXIT_GAME -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, PlayerExitPayload::class.java)
                    val player = gameService.findPlayerById(payload.playerId)
                    if (player != null) {
                        gameService.disconnectPlayer(player)
                        broadcastPlayers()
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

    private fun broadcastPlayers() {
        val players = gameService.getAllPlayers()
        broadcast(OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, players))
    }

    private fun broadcastPlayersSwap(player1Id: String, player2Id: String) {
        broadcast(OutcomeMessage(OutcomeAction.UPDATE_SWAPPED_PLAYERS, Pair(player1Id, player2Id)))
    }

    private fun sendToPlayer(session: WebSocketSession, message: OutcomeMessage<*>) {
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }

    private fun sendAllPlayersStartPositions() {
        val players = gameService.getAllPlayersShuffled()
        broadcast(OutcomeMessage(OutcomeAction.START_QUEUE, StartQueueOutcomeData(players)))
    }

    private fun sendAllPlayersFinalQueue() {
        val players = gameService.getAllPlayersShuffledQueue()
        broadcast(OutcomeMessage(OutcomeAction.FINAL_QUEUE, FinalQueueOutcomeData(players)))
    }

    private fun broadcast(message: OutcomeMessage<*>) {
        val serializedMessage = objectMapper.writeValueAsString(message)
        sessions.forEach { it.sendMessage(TextMessage(serializedMessage)) }
    }

    private fun changeRound(round: ROUND) {
        if (gameService.getCurrentRound() != ROUND.SWAP) {
            gameService.resetCurrentTurnPlayer()
            broadcast(
                OutcomeMessage(
                    OutcomeAction.PLAYER_TURN,
                    null
                )
            )
        }
        gameService.setCurrentRound(round)
        broadcast(OutcomeMessage(OutcomeAction.ROUND_NAME, round.value))
    }

    private fun sendStartData(session: WebSocketSession, player: Player) {
        sendToPlayer(session, OutcomeMessage(OutcomeAction.ROUND_NAME, gameService.getCurrentRound().value))
        sendToPlayer(session, OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, gameService.getAllPlayers()))
        if (gameService.getCurrentRound() == ROUND.END) {
            sendToPlayer(session, OutcomeMessage(OutcomeAction.GAME_FINISH, gameService.getAllGifts()))
        }
        sendToPlayer(session, OutcomeMessage(OutcomeAction.PLAYER_TURN, gameService.getCurrentTurnPlayer()))
        sendToPlayer(session, OutcomeMessage(OutcomeAction.JOINED_PLAYER, player))
    }

    private fun sendAllPlayersEndGame() {
        val players = gameService.getAllPlayers()
        broadcast(OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, players))
        val round = gameService.getCurrentRound()
        broadcast(OutcomeMessage(OutcomeAction.ROUND_NAME, round.value))
        val currentTurnPlayer = gameService.getCurrentTurnPlayer()
        broadcast(OutcomeMessage(OutcomeAction.PLAYER_TURN, currentTurnPlayer))
    }

    private fun broadcastGameFinal() {
        val gifts = gameService.getAllGifts()
        broadcast(OutcomeMessage(OutcomeAction.GAME_FINISH, gifts))
    }
}

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