package com.example.prsentswitcherbackend.service

import com.example.prsentswitcherbackend.handler.ROUND
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class Player(
    val id: String,
    val name: String,
    var position: Int? = null,
    var turn: Int? = null,
    var isDisconnected: Boolean = false
)

data class Gift(val position: Int, val content: String)

@Service
class GameService {

    private val players = ConcurrentHashMap<String, Player>()
    private val currentRound = AtomicInteger(0)

    //    private val gifts = listOf(
//        Gift(0, "Подарок где вот такое название 1"),
//        Gift(1, "Подарок где  вообще какое-то огромное название что даже не влезает 2"),
//        Gift(2, "Подарок 3"),
//        Gift(3, "Подарок 4"),
//        Gift(4, "Подарок 5"),
//        Gift(5, "Подарок 6"),
//        Gift(6, "Подарок 7"),
//        Gift(7, "Подарок 8"),
//    )
    private lateinit var gifts: List<Gift>
    private val playerIdCounter = AtomicInteger(1)
    private var currentTurnPlayer: Player? = null

    fun initializeItems() {
        ItemProcessor.readFile("presents.txt")
        println("Старт игры для ${players.size} игроков")
        val items = ItemProcessor.findItemsWithSum(players.size, 10000)
        val totalSum = items.sumOf { it.price }
        println("Total sum: $totalSum")
        gifts = items.mapIndexed { index, item -> Gift(index, item.name) }
        println("Gifts: $gifts")
    }

    fun addPlayer(name: String, playerId: String?): Player? {
        if (playerId != null && players.containsKey(playerId)) {
            val player = players.values.first { it.id == playerId }
            player.isDisconnected = false
            return player
        }
//        if (currentRound.get() != 0) {
//            return null
//        }
        if (players.size >= 9) {
            return null
        }
        if (players.values.any { it.name == name }) {
            return null
        }
        val position = playerIdCounter.getAndIncrement()
        val id = "player-$position"
        val player = Player(id, name, null)
        players[id] = player
        return player
    }

    fun findNextPlayerTurn(currentPlayerIdTurn: String): Player? {
        println("currentPlayerIdTurn = $currentPlayerIdTurn")
        val currentPlayer = players[currentPlayerIdTurn] ?: return null
        println("currentPlayer = $currentPlayer")
        val nextPlayer = players.values.firstOrNull { it.turn == currentPlayer.turn?.plus(1) }
        println("nextPlayer = $nextPlayer")
        currentTurnPlayer = nextPlayer
        return nextPlayer
    }

    fun findFirstPlayerTurn(): Player {
        val firstPlayer = players.values.first { it.turn == 0 }
        currentTurnPlayer = firstPlayer
        return firstPlayer
    }

    fun resetCurrentTurnPlayer() {
        currentTurnPlayer = null
    }

    fun getCurrentTurnPlayer(): Player? = currentTurnPlayer

    fun getAllPlayers(): List<Player> = players.values.toList().sortedBy { it.position }

    fun getAllPlayersShuffled(): List<Player> {
        shufflePlayers()
        return players.values.toList().sortedBy { it.position }
    }

    fun getAllPlayersShuffledQueue(): List<Player> {
        shufflePlayerTurns()
        return players.values.toList().sortedBy { it.position }
    }

    private fun shufflePlayerTurns() {
        val shuffledPlayers = players.values.shuffled()
        shuffledPlayers.forEachIndexed { index, player -> player.turn = index }
    }

    private fun shufflePlayers() {
        val shuffledPlayers = players.values.shuffled()
        shuffledPlayers.forEachIndexed { index, player ->
            player.position = index
            player.turn = index
        }
    }

    fun swapPlayers(playerId1: String, playerId2: String) {
        val player1 = players[playerId1] ?: return
        val player2 = players[playerId2] ?: return
        val tempPosition = player1.position
        player1.position = player2.position
        player2.position = tempPosition
    }

    fun viewGift(playerId: String): Gift? {
        if (getCurrentRound() == ROUND.SWAP) {
            return null
        }
        println("Players: $players and playerId: $playerId")
        println("Gifts: $gifts")
        val player = players[playerId] ?: return null
        println("Player: $player")
        val gift = gifts.firstOrNull { it.position == player.position }
        println("Gift: $gift")
        return gift
    }

    fun findPlayerById(playerId: String): Player? = players[playerId]

    @OptIn(DelicateCoroutinesApi::class)
    fun disconnectPlayer(player: Player) {
        players.values.first { it.id == player.id }.isDisconnected = true
        GlobalScope.launch {
            delay(30000) // 30 seconds delay
            if (players[player.id]?.isDisconnected == true) {
                players.remove(player.id)
            }
        }
    }

    fun getCurrentRound(): ROUND {
        println("currentRound.get() = ${currentRound.get()}")
        println("Return ROUND.entries[currentRound.get()] = ${ROUND.entries[currentRound.get()]}")
        return ROUND.entries[currentRound.get()]
    }

    fun setCurrentRound(round: ROUND) {
        println("currentRound.set(round.ordinal) = ${round.ordinal}")
        currentRound.set(round.ordinal)
    }

    fun endGame() {
        players.clear()
        currentRound.set(0)
        playerIdCounter.set(1)
        currentTurnPlayer = null
    }

    fun setPlayerTurns() {
        players.values.forEach { player -> player.turn = player.position }
    }

    fun getAllGifts(): List<String> {
        println("gifts = $gifts")
        println("players = $players")
        val result = players.values.map {
            val gift = gifts.firstOrNull { gift -> gift.position == it.position }
            "${it.name} - ${gift?.content ?: "нет подарка"}"
        }
        println("result = $result")
        return result
    }

}