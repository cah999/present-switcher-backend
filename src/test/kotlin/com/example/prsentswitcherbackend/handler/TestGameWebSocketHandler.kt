package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.command.GameActionCommandFactory
import com.example.prsentswitcherbackend.service.MessageService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession


class TestGameWebSocketHandler(
    messageService: MessageService,
    gameActionCommandFactory: GameActionCommandFactory
) : GameWebSocketHandler(messageService, gameActionCommandFactory) {
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        super.handleTextMessage(session, message)
    }
}