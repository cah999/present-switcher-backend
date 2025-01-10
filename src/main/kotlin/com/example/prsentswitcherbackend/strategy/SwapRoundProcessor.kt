package com.example.prsentswitcherbackend.strategy

import com.example.prsentswitcherbackend.model.income.RoundChangePayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import org.springframework.web.socket.WebSocketSession

class SwapRoundProcessor(
    private val gameService: GameService,
    private val messageService: MessageService
) : RoundProcessor {
    override fun process(session: WebSocketSession, payload: RoundChangePayload) {
        val firstPlayerTurn = gameService.findAndSetFirstPlayerTurn()
        messageService.broadcastCurrentTurnPlayer(firstPlayerTurn)
    }
}