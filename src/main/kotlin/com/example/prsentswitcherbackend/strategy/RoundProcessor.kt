package com.example.prsentswitcherbackend.strategy

import com.example.prsentswitcherbackend.model.income.RoundChangePayload
import org.springframework.web.socket.WebSocketSession

interface RoundProcessor {
    fun process(session: WebSocketSession, payload: RoundChangePayload)
}