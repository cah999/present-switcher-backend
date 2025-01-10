package com.example.prsentswitcherbackend.strategy

import com.example.prsentswitcherbackend.model.income.RoundChangePayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import org.springframework.web.socket.WebSocketSession

class StartRoundProcessor(
    private val gameService: GameService,
    private val messageService: MessageService
) : RoundProcessor {
    override fun process(session: WebSocketSession, payload: RoundChangePayload) {
        gameService.initializeItems()
        messageService.broadcastStartPositions(gameService.getAllPlayersShuffled())
    }
}