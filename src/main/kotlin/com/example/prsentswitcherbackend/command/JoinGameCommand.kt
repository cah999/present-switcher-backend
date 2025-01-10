package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.income.IncomeMessage
import com.example.prsentswitcherbackend.model.income.JoinGamePayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.springframework.web.socket.WebSocketSession

class JoinGameCommand(
    private val messageService: MessageService,
    private val gameService: GameService,
) : GameActionCommand {

    override fun execute(session: WebSocketSession, incomeMessage: IncomeMessage<*>) {
        val payload = JsonUtils.convertData(incomeMessage.data, JoinGamePayload::class.java)
        val player = gameService.addPlayer(payload.name, payload.playerId)
        println("Joined player $player")
        if (player != null) {
            val allPlayers = gameService.getAllPlayers()
            val currentRound = gameService.getCurrentRound()
            val currentTurnPlayer = gameService.getCurrentTurnPlayer()
            val allGifts = gameService.getAllGifts()
            messageService.sendStartDataToPlayer(
                session, player, currentRound, currentTurnPlayer, allPlayers, allGifts
            )
            messageService.broadcastPlayers(allPlayers)
        }
    }
}