package com.example.prsentswitcherbackend.model.enums

enum class ROUND(val value: String) {
    WAITING("ожидание участников"),
    START("распределение подарков"),
    TALK("обсуждение"),
    SWAP("обмен подарками"),
    FINAL("очередь на финал"),
    END("итоги"),
}