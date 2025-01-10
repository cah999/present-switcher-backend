package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.income.IncomeMessage
import com.example.prsentswitcherbackend.model.income.ViewGiftPayload
import com.example.prsentswitcherbackend.service.GameService
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession

class ViewGiftCommand(
    private val messageService: MessageService,
    private val gameService: GameService,
) : GameActionCommand {

    private val logger: Logger = LoggerFactory.getLogger(ViewGiftCommand::class.java)

    override fun execute(session: WebSocketSession, incomeMessage: IncomeMessage<*>) {
        val payload = JsonUtils.convertData(incomeMessage.data, ViewGiftPayload::class.java)
        val giftContent = gameService.viewGift(payload.playerId)
        logger.info("Player ${payload.playerId} viewed gift $giftContent")
        messageService.sendGiftToPlayer(session, giftContent)
    }
}