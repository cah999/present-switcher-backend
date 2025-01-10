package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.model.enums.IncomeAction
import com.example.prsentswitcherbackend.model.enums.OutcomeAction
import com.example.prsentswitcherbackend.model.enums.ROUND
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.model.Player
import com.example.prsentswitcherbackend.model.income.*
import com.example.prsentswitcherbackend.model.outcome.FinalQueueOutcomeData
import com.example.prsentswitcherbackend.model.outcome.OutcomeMessage
import com.example.prsentswitcherbackend.model.outcome.StartQueueOutcomeData
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

    private val playerSessions = CopyOnWriteArrayList<WebSocketSession>()
    private val logger: Logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java)


    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("New connection established: $session")
        playerSessions.add(session)
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
                        sendStartDataToPlayer(session, player)
                        broadcastPlayers()
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
                    broadcastMessage(turnMessage)
                }

                IncomeAction.VIEW_GIFT -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, ViewGiftPayload::class.java)
                    val giftContent = gameService.viewGift(payload.playerId)
                    println("giftContent: $giftContent")
                    sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.VIEW_GIFT, giftContent))
                }

                IncomeAction.ROUND_CHANGED -> {
                    val payload = objectMapper.convertValue(incomingMessage.data, RoundChangePayload::class.java)
                    changeRound(payload.newRound)
                    when (payload.newRound) {
                        ROUND.WAITING -> {
                            gameService.endGame()
                            broadcastEndGame()
                        }

                        ROUND.START -> {
                            gameService.initializeItems()
                            broadcastStartPositions()
                        }

                        ROUND.TALK -> {
                            gameService.setPlayerTurns()
                            broadcastPlayers()
                        }

                        ROUND.SWAP -> {
                            val turnMessage = OutcomeMessage(
                                OutcomeAction.PLAYER_TURN,
                                gameService.findFirstPlayerTurn()
                            )
                            broadcastMessage(turnMessage)
                        }

                        ROUND.FINAL -> {
                            broadcastFinalQueue()
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
        playerSessions.remove(session)
    }

    private fun broadcastMessage(message: OutcomeMessage<*>) {
        val serializedMessage = objectMapper.writeValueAsString(message)
        playerSessions.forEach { it.sendMessage(TextMessage(serializedMessage)) }
    }

    private fun sendMessageToPlayer(session: WebSocketSession, message: OutcomeMessage<*>) {
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }

    private fun broadcastPlayersSwap(player1Id: String, player2Id: String) {
        broadcastMessage(OutcomeMessage(OutcomeAction.UPDATE_SWAPPED_PLAYERS, Pair(player1Id, player2Id)))
    }


    private fun changeRound(round: ROUND) {
        if (gameService.getCurrentRound() != ROUND.SWAP) {
            gameService.resetCurrentTurnPlayer()
            broadcastMessage(OutcomeMessage(OutcomeAction.PLAYER_TURN, null))
        }
        gameService.setCurrentRound(round)
        broadcastMessage(OutcomeMessage(OutcomeAction.ROUND_NAME, round.value))
    }

    private fun sendStartDataToPlayer(session: WebSocketSession, player: Player) {
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.ROUND_NAME, gameService.getCurrentRound().value))
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, gameService.getAllPlayers()))
        if (gameService.getCurrentRound() == ROUND.END) {
            sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.GAME_FINISH, gameService.getAllGifts()))
        }
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.PLAYER_TURN, gameService.getCurrentTurnPlayer()))
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.JOINED_PLAYER, player))
    }

    private fun broadcastGameFinal() {
        val gifts = gameService.getAllGifts()
        broadcastMessage(OutcomeMessage(OutcomeAction.GAME_FINISH, gifts))
    }

    private fun broadcastPlayers() {
        val players = gameService.getAllPlayers()
        broadcastMessage(OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, players))
    }

    private fun broadcastStartPositions() {
        val players = gameService.getAllPlayersShuffled()
        broadcastMessage(OutcomeMessage(OutcomeAction.START_QUEUE, StartQueueOutcomeData(players)))
    }

    private fun broadcastFinalQueue() {
        val players = gameService.getAllPlayersShuffledQueue()
        broadcastMessage(OutcomeMessage(OutcomeAction.FINAL_QUEUE, FinalQueueOutcomeData(players)))
    }

    private fun broadcastCurrentTurnPlayer() {
        val currentTurnPlayer = gameService.getCurrentTurnPlayer()
        broadcastMessage(OutcomeMessage(OutcomeAction.PLAYER_TURN, currentTurnPlayer))
    }

    private fun broadcastCurrentRound() {
        val round = gameService.getCurrentRound()
        broadcastMessage(OutcomeMessage(OutcomeAction.ROUND_NAME, round.value))
    }

    private fun broadcastEndGame() {
        broadcastPlayers()
        broadcastCurrentRound()
        broadcastCurrentTurnPlayer()
    }
}