package com.example.prsentswitcherbackend.service

import com.example.prsentswitcherbackend.model.Player
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class PlayerService {
    private val players = ConcurrentHashMap<String, Player>()
    private val newPlayerId = AtomicInteger(1)

    fun addPlayer(name: String, playerId: String?): Player? {
        if (isRejoiningPlayer(playerId)) {
            return rejoinPlayer(playerId!!)
        }
        if (isPlayerLimitReached() || isPlayerNameTaken(name)) {
            return null
        }
        return createNewPlayer(name)
    }

    fun setPlayerTurns() {
        players.values.forEach { player -> player.turn = player.position }
    }

    fun getPlayersCount(): Int = players.size

    fun findNextPlayerTurn(currentPlayerIdTurn: String): Player? {
        val currentPlayer = players[currentPlayerIdTurn] ?: return null
        return players.values.firstOrNull { it.turn == currentPlayer.turn?.plus(1) }
    }

    fun swapPlayers(playerId1: String, playerId2: String) {
        val player1 = players[playerId1] ?: return
        val player2 = players[playerId2] ?: return
        val tempPosition = player1.position
        player1.position = player2.position
        player2.position = tempPosition
    }

    fun findPlayerById(playerId: String): Player? = players[playerId]

    fun getAllPlayers(): List<Player> = players.values.toList().sortedBy { it.position }

    fun shufflePlayers(): List<Player> {
        val shuffledPlayers = players.values.shuffled()
        shuffledPlayers.forEachIndexed { index, player ->
            player.position = index
            player.turn = index
        }
        return shuffledPlayers
    }

    fun shufflePlayersQueue(): List<Player> {
        val shuffledPlayers = players.values.shuffled()
        shuffledPlayers.forEachIndexed { index, player -> player.turn = index }
        return shuffledPlayers.toList().sortedBy { it.position }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun disconnectPlayer(player: Player) {
        players.values.first { it.id == player.id }.disconnect()
        GlobalScope.launch {
            delay(30000) // 30 seconds delay
            if (players[player.id]?.isConnected() == false) {
                players.remove(player.id)
            }
        }
    }

    fun clearPlayers() {
        players.clear()
        newPlayerId.set(1)
    }


    private fun isRejoiningPlayer(playerId: String?): Boolean {
        return playerId != null && players.containsKey(playerId)
    }

    private fun rejoinPlayer(playerId: String): Player {
        val player = players.values.first { it.id == playerId }
        player.connect()
        return player
    }

    private fun isPlayerLimitReached(): Boolean {
        return players.size >= MAX_PLAYERS
    }

    private fun isPlayerNameTaken(name: String): Boolean {
        return players.values.any { it.name == name }
    }

    private fun createNewPlayer(name: String): Player {
        val position = newPlayerId.getAndIncrement()
        val id = "player-$position"
        val player = Player(id, name, null)
        players[id] = player
        return player
    }

    companion object {
        private const val MAX_PLAYERS = 9
    }
}