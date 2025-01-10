package com.example.prsentswitcherbackend.service

import com.example.prsentswitcherbackend.model.Gift
import com.example.prsentswitcherbackend.model.Player
import org.springframework.stereotype.Service

@Service
class GiftService {
    private lateinit var gifts: List<Gift>

    fun initializeGifts(playersCount: Int, price: Int) {
        GiftSelector.readFile("presents.txt")
        val items = GiftSelector.findItemsWithSum(playersCount, price)
        gifts = items.mapIndexed { index, item -> item.toGift(index) }
    }

    fun getGiftByPosition(position: Int?): Gift? = gifts.firstOrNull { it.position == position }


    fun getAllGifts(players: List<Player>): List<String> {
        return players.map {
            val gift = gifts.firstOrNull { gift -> gift.position == it.position }
            "${it.name} - ${gift?.isEmpty() ?: "нет подарка"}"
        }
    }

    fun resetGifts() {
        gifts = emptyList()
        GiftSelector.resetItems()
    }
}