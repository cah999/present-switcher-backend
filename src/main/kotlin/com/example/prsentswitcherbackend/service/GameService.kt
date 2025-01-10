package com.example.prsentswitcherbackend.service

import com.example.prsentswitcherbackend.model.Gift
import com.example.prsentswitcherbackend.model.Player
import com.example.prsentswitcherbackend.model.enums.ROUND
import org.springframework.stereotype.Service

@Service
class GameService(
    private val playerService: PlayerService,
    private val roundService: RoundService,
    private val giftService: GiftService
) {
    private var currentTurnPlayer: Player? = null

    fun initializeItems() {
        val playersCount = playerService.getPlayersCount()
        giftService.initializeGifts(playersCount, 10000)
    }

    fun findAndSetNextPlayerTurn(currentPlayerIdTurn: String): Player? {
        currentTurnPlayer = playerService.findNextPlayerTurn(currentPlayerIdTurn)
        return currentTurnPlayer
    }

    fun findAndSetFirstPlayerTurn(): Player {
        val firstPlayer = playerService.getAllPlayers().first { it.turn == 0 }
        currentTurnPlayer = firstPlayer
        return firstPlayer
    }

    fun viewGift(playerId: String): Gift? {
        if (getCurrentRound() == ROUND.SWAP) {
            return null
        }
        val player = playerService.findPlayerById(playerId) ?: return null
        return giftService.getGiftByPosition(player.position)
    }

    fun endGame() {
        playerService.clearPlayers()
        roundService.resetRound()
        giftService.resetGifts()
        currentTurnPlayer = null
    }

    fun resetCurrentTurnPlayer() {
        currentTurnPlayer = null
    }

    fun addPlayer(name: String, playerId: String?): Player? = playerService.addPlayer(name, playerId)

    fun getCurrentTurnPlayer(): Player? = currentTurnPlayer

    fun getAllPlayers(): List<Player> = playerService.getAllPlayers()

    fun getAllPlayersShuffled(): List<Player> = playerService.shufflePlayers()

    fun getAllPlayersShuffledQueue(): List<Player> = playerService.shufflePlayersQueue()

    fun swapPlayers(playerId1: String, playerId2: String) = playerService.swapPlayers(playerId1, playerId2)

    fun findPlayerById(playerId: String): Player? = playerService.findPlayerById(playerId)

    fun disconnectPlayer(player: Player) = playerService.disconnectPlayer(player)

    fun getCurrentRound(): ROUND = roundService.getCurrentRound()

    fun setCurrentRound(round: ROUND) = roundService.setCurrentRound(round)

    fun setPlayerTurns() = playerService.setPlayerTurns()

    fun getAllGifts(): List<String> = giftService.getAllGifts(playerService.getAllPlayers())
}