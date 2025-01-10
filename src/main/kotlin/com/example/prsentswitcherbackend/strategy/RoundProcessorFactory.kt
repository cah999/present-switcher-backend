package com.example.prsentswitcherbackend.strategy

import com.example.prsentswitcherbackend.model.enums.ROUND
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import org.springframework.stereotype.Component

@Component
class RoundProcessorFactory(
    private val gameService: GameService,
    private val messageService: MessageService
) {
    fun getProcessor(round: ROUND): RoundProcessor =
        when (round) {
            ROUND.WAITING -> WaitingRoundProcessor(gameService, messageService)
            ROUND.START -> StartRoundProcessor(gameService, messageService)
            ROUND.TALK -> TalkRoundProcessor(gameService, messageService)
            ROUND.SWAP -> SwapRoundProcessor(gameService, messageService)
            ROUND.FINAL -> FinalRoundProcessor(gameService, messageService)
            ROUND.END -> EndRoundProcessor(gameService, messageService)
        }
}