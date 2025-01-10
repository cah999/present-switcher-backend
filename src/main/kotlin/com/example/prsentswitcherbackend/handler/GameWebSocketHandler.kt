package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.command.GameActionCommandFactory
import com.example.prsentswitcherbackend.model.income.IncomeMessage
import com.example.prsentswitcherbackend.service.MessageService
import com.example.prsentswitcherbackend.utils.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Controller
class GameWebSocketHandler(
    private val messageService: MessageService,
    private val gameActionCommandFactory: GameActionCommandFactory,
) : TextWebSocketHandler() {

    private val logger: Logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java)


    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("New connection established: $session")
        messageService.addPlayerSession(session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Error handling transport in session: $session, exc ${exception.message}", exception)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            logger.info("Received message: ${message.payload}")
            val incomingMessage = JsonUtils.fromJson(message.payload, IncomeMessage::class.java)
            logger.info("Received message: $incomingMessage")

            val command = gameActionCommandFactory.createCommand(incomingMessage.action)
            command.execute(session, incomingMessage)

        } catch (e: Exception) {
            logger.error("Error handling message", e)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        logger.info("Connection closed: $session")
        messageService.removePlayerSession(session)
    }
}