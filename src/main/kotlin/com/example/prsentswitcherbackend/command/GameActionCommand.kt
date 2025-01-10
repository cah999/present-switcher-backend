package com.example.prsentswitcherbackend.command

import com.example.prsentswitcherbackend.model.income.IncomeMessage
import org.springframework.web.socket.WebSocketSession

interface GameActionCommand {
    fun execute(session: WebSocketSession, incomeMessage: IncomeMessage<*>)
}