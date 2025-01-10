package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.enums.IncomeAction
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.strategy.RoundProcessorFactory
import org.springframework.stereotype.Component

@Component
class GameActionCommandFactory(
    private val messageService: MessageService,
    private val gameService: GameService,
    private val roundFactory: RoundProcessorFactory
) {
    fun createCommand(action: IncomeAction) =
        when (action) {
            IncomeAction.JOIN_GAME -> JoinGameCommand(messageService, gameService)
            IncomeAction.SWAP_PLAYERS -> SwapPlayersCommand(messageService, gameService)
            IncomeAction.VIEW_GIFT -> ViewGiftCommand(messageService, gameService)
            IncomeAction.ROUND_CHANGED -> RoundChangedCommand(messageService, gameService, roundFactory)
            IncomeAction.EXIT_GAME -> ExitGameCommand(messageService, gameService)
        }
}