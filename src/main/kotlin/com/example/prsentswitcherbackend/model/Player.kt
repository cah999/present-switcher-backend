package com.example.prsentswitcherbackend.model

data class Player(
    val id: String,
    val name: String,
    var position: Int? = null,
    var turn: Int? = null,
    var isDisconnected: Boolean = false
)