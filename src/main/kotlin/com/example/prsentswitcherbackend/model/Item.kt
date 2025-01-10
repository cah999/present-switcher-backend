package com.example.prsentswitcherbackend.model

data class Item(val name: String, val price: Int) {

    fun toGift(position: Int): Gift {
        return Gift(position, name)
    }
}