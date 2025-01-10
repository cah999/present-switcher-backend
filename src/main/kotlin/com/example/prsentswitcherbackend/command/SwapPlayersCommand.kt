package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.income.IncomeMessage
import com.example.prsentswitcherbackend.model.income.MovePlayerPayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.springframework.web.socket.WebSocketSession

class SwapPlayersCommand(
    private val messageService: MessageService,
    private val gameService: GameService,
) : GameActionCommand {

    override fun execute(session: WebSocketSession, incomeMessage: IncomeMessage<*>) {
        val payload = JsonUtils.convertData(incomeMessage.data, MovePlayerPayload::class.java)
        gameService.swapPlayers(payload.player1Id, payload.player2Id)
        messageService.broadcastPlayersSwap(payload.player1Id, payload.player2Id)
        val nextPlayerTurn = gameService.findAndSetNextPlayerTurn(currentPlayerIdTurn = payload.player1Id)
        messageService.broadcastCurrentTurnPlayer(nextPlayerTurn)
    }
}