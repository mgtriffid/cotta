package com.mgtriffid.games.panna.shared

import com.mgtriffid.games.cotta.core.CottaGame
import java.lang.IllegalStateException

class PannaGame: CottaGame {
    override fun update() {
        println("Called update of PannaGame") // why
    }

    override fun initialState(): Any {
        return 200
    }

    override fun calculateNonPlayerInput(state: Any): Any {
        val direction = if (java.util.Random().nextBoolean()) {
            1
        } else {
            -1
        }
        @Suppress("KotlinConstantConditions")
        return when {
            direction == 1 && (state as Int) >= 200 -> 9
            direction == -1 && (state as Int) >= 200 -> -11
            direction == 1 && (state as Int) < 200 -> 11
            direction == -1 && (state as Int) < 200 -> -9
            else -> throw IllegalStateException()
        }
    }

    override fun applyInput(state: Any, input: Any): Any {
        return (state as Int) + (input as Int)
    }
}
