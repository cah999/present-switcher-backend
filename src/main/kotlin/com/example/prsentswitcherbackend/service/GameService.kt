package com.example.prsentswitcherbackend.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class Player(val id: String, val name: String, var position: Int? = null, var turn: Int? = null)
data class Gift(val id: Int, val content: String, val price: Int = 100)

@Service
class GameService {

    private val players = ConcurrentHashMap<String, Player>()
    private val gifts = List(7) { Gift(it + 1, "Подарок #${it + 1}") }
    private val playerIdCounter = AtomicInteger(1)

    fun addPlayer(name: String): Player {
        val position = playerIdCounter.getAndIncrement()
        val id = "player-$position"
        val player = Player(id, name, null)
        players[id] = player
        return player
    }

    fun findNextPlayerTurn(currentPlayerIdTurn: String): Player? {
        val currentPlayer = players[currentPlayerIdTurn] ?: return null
        val nextPlayer = players.values.firstOrNull { it.position == currentPlayer.position?.plus(1) }
        return nextPlayer
    }

    fun getAllPlayers(): List<Player> = players.values.toList()

    fun getAllPlayersShuffled(): List<Player> {
        shufflePlayers()
        return players.values.toList()
    }

    fun getAllPlayersShuffledQueue(): List<Player> {
        shufflePlayerTurns()
        return players.values.toList()
    }

    private fun shufflePlayerTurns() {
        val shuffledPlayers = players.values.shuffled()
        shuffledPlayers.forEachIndexed { index, player -> player.turn = index }
    }

    private fun shufflePlayers() {
        val shuffledPlayers = players.values.shuffled()
        shuffledPlayers.forEachIndexed { index, player -> player.position = index }
    }

    fun swapPlayers(playerId1: String, playerId2: String) {
        val player1 = players[playerId1] ?: return
        val player2 = players[playerId2] ?: return
        val tempPosition = player1.position
        player1.position = player2.position
        player2.position = tempPosition
    }

    fun viewGift(playerId: String): String? {
        val player = players[playerId] ?: return null
        val gift = gifts.firstOrNull { it.id == player.position }
        return gift?.content
    }
}
