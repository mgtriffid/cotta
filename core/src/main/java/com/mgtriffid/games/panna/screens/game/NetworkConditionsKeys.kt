package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.Input.Keys

object NetworkConditionsKeys {
    val sending = ConditionsKeys(
        decreaseMinLatency = Keys.Y,
        increaseMinLatency = Keys.U,
        decreaseMaxLatency = Keys.H,
        increaseMaxLatency = Keys.J,
        decreasePacketLoss = Keys.N,
        increasePacketLoss = Keys.M
    )

    val receiving = ConditionsKeys(
        decreaseMinLatency = Keys.I,
        increaseMinLatency = Keys.O,
        decreaseMaxLatency = Keys.K,
        increaseMaxLatency = Keys.L,
        decreasePacketLoss = Keys.COMMA,
        increasePacketLoss = Keys.PERIOD
    )

    class ConditionsKeys(
        val decreaseMinLatency: Int,
        val increaseMinLatency: Int,
        val decreaseMaxLatency: Int,
        val increaseMaxLatency: Int,
        val decreasePacketLoss: Int,
        val increasePacketLoss: Int
    )
}
