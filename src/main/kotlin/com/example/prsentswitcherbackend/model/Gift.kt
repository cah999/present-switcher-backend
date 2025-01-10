package com.example.prsentswitcherbackend.model

data class Gift(
    val position: Int,
    var content: String
) {
    fun isEmpty(): Boolean {
        return content.isBlank()
    }
}