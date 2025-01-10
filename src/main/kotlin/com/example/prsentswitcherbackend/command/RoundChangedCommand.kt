package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.enums.ROUND
import com.example.prsentswitcherbackend.model.income.IncomeMessage
import com.example.prsentswitcherbackend.model.income.RoundChangePayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.strategy.RoundProcessorFactory
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.springframework.web.socket.WebSocketSession

class RoundChangedCommand(
    private val messageService: MessageService,
    private val gameService: GameService,
    private val processor: RoundProcessorFactory
) : GameActionCommand {

    override fun execute(session: WebSocketSession, incomeMessage: IncomeMessage<*>) {
        val payload = JsonUtils.convertData(incomeMessage.data, RoundChangePayload::class.java)
        changeRound(payload.newRound)
        messageService.broadcastCurrentRound(payload.newRound)
        processor.getProcessor(payload.newRound).process(session, payload)
    }

    private fun changeRound(round: ROUND) {
        if (gameService.getCurrentRound() != ROUND.SWAP) {
            gameService.resetCurrentTurnPlayer()
            messageService.broadcastCurrentTurnPlayer(null)
        }
        gameService.setCurrentRound(round)
        messageService.broadcastCurrentRound(round)
    }
}