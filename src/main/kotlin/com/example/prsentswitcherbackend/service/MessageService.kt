package com.example.prsentswitcherbackend.service

import com.example.prsentswitcherbackend.model.Gift
import com.example.prsentswitcherbackend.model.Player
import com.example.prsentswitcherbackend.model.enums.OutcomeAction
import com.example.prsentswitcherbackend.model.enums.ROUND
import com.example.prsentswitcherbackend.model.outcome.FinalQueueOutcomeData
import com.example.prsentswitcherbackend.model.outcome.OutcomeMessage
import com.example.prsentswitcherbackend.model.outcome.StartQueueOutcomeData
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.CopyOnWriteArrayList

@Service
class MessageService {

    private val playerSessions: CopyOnWriteArrayList<WebSocketSession> = CopyOnWriteArrayList()

    fun addPlayerSession(session: WebSocketSession) {
        playerSessions.add(session)
    }

    fun removePlayerSession(session: WebSocketSession) {
        playerSessions.remove(session)
    }

    fun broadcastPlayersSwap(player1Id: String, player2Id: String) {
        broadcastMessage(OutcomeMessage(OutcomeAction.UPDATE_SWAPPED_PLAYERS, Pair(player1Id, player2Id)))
    }

    // todo refactor attributes
    fun sendStartDataToPlayer(
        session: WebSocketSession,
        joinedPlayer: Player,
        currentRound: ROUND,
        currentTurnPlayer: Player?,
        players: List<Player>,
        gifts: List<String>,
    ) {
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.ROUND_NAME, currentRound.value))
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, players))
        if (currentRound == ROUND.END) {
            sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.GAME_FINISH, gifts))
        }
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.PLAYER_TURN, currentTurnPlayer))
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.JOINED_PLAYER, joinedPlayer))
    }

    fun broadcastGameFinal(gifts: List<String>) {
        broadcastMessage(OutcomeMessage(OutcomeAction.GAME_FINISH, gifts))
    }

    fun broadcastPlayers(players: List<Player>) {
        broadcastMessage(OutcomeMessage(OutcomeAction.UPDATE_PLAYERS, players))
    }

    fun broadcastCurrentTurnPlayer(currentTurnPlayer: Player?) {
        broadcastMessage(OutcomeMessage(OutcomeAction.PLAYER_TURN, currentTurnPlayer))
    }

    fun broadcastCurrentRound(round: ROUND) {
        broadcastMessage(OutcomeMessage(OutcomeAction.ROUND_NAME, round.value))
    }

    fun sendGiftToPlayer(session: WebSocketSession, giftContent: Gift?) {
        sendMessageToPlayer(session, OutcomeMessage(OutcomeAction.VIEW_GIFT, giftContent))
    }

    fun broadcastStartPositions(players: List<Player>) {
        broadcastMessage(OutcomeMessage(OutcomeAction.START_QUEUE, StartQueueOutcomeData(players)))
    }

    fun broadcastFinalQueue(players: List<Player>) {
        broadcastMessage(OutcomeMessage(OutcomeAction.FINAL_QUEUE, FinalQueueOutcomeData(players)))
    }

    private fun broadcastMessage(message: OutcomeMessage<*>) {
        val serializedMessage = JsonUtils.toJson(message)
        playerSessions.forEach { it.sendMessage(TextMessage(serializedMessage)) }
    }

    private fun sendMessageToPlayer(session: WebSocketSession, message: OutcomeMessage<*>) {
        session.sendMessage(TextMessage(JsonUtils.toJson(message)))
    }
}