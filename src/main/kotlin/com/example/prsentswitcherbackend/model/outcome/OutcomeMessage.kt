package com.example.prsentswitcherbackend.model.outcome

import com.example.prsentswitcherbackend.model.enums.OutcomeAction

data class OutcomeMessage<T>(
    val action: OutcomeAction,
    val data: T
)