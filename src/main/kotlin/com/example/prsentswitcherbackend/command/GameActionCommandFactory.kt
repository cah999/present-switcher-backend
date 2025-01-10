package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.enums.IncomeAction
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import org.springframework.stereotype.Component

@Component
class GameActionCommandFactory(
    private val messageService: MessageService,
    private val gameService: GameService,
) {
    fun createCommand(action: IncomeAction) =
        when (action) {
            IncomeAction.JOIN_GAME -> JoinGameCommand(messageService, gameService)
            IncomeAction.SWAP_PLAYERS -> SwapPlayersCommand(messageService, gameService)
            IncomeAction.VIEW_GIFT -> ViewGiftCommand(messageService, gameService)
            IncomeAction.ROUND_CHANGED -> RoundChangedCommand(messageService, gameService)
            IncomeAction.EXIT_GAME -> ExitGameCommand(messageService, gameService)
        }
}