package com.example.prsentswitcherbackend.service

import com.example.prsentswitcherbackend.model.enums.ROUND
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class RoundService {
    private val currentRound = AtomicInteger(0)

    fun getCurrentRound(): ROUND = ROUND.entries[currentRound.get()]

    fun setCurrentRound(round: ROUND) {
        currentRound.set(round.ordinal)
    }

    fun resetRound() {
        currentRound.set(0)
    }
}