package com.example.prsentswitcherbackend.handler

import com.example.prsentswitcherbackend.service.GameService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

class TestGameWebSocketHandler(
    gameService: GameService,
    objectMapper: ObjectMapper
) : GameWebSocketHandler(gameService, objectMapper) {
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        super.handleTextMessage(session, message)
    }
}