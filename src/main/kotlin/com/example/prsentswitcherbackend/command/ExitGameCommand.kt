package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.income.IncomeMessage
import com.example.prsentswitcherbackend.model.income.PlayerExitPayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.springframework.web.socket.WebSocketSession

class ExitGameCommand(
    private val messageService: MessageService,
    private val gameService: GameService,
) : GameActionCommand {

    override fun execute(session: WebSocketSession, incomeMessage: IncomeMessage<*>) {
        val payload = JsonUtils.convertData(incomeMessage.data, PlayerExitPayload::class.java)
        val player = gameService.findPlayerById(payload.playerId)
        if (player != null) {
            gameService.disconnectPlayer(player)
            messageService.broadcastPlayers(gameService.getAllPlayers())
        }
    }
}