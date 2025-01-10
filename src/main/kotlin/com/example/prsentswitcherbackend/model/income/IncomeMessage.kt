package com.example.prsentswitcherbackend.model.income

import com.example.prsentswitcherbackend.model.enums.IncomeAction

data class IncomeMessage<T>(
    val action: IncomeAction,
    val data: T
)
