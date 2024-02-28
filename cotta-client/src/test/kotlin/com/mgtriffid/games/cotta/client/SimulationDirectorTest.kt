package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.CurrentState
import com.mgtriffid.games.cotta.client.impl.SimulationDirectorImpl
import com.mgtriffid.games.cotta.core.entities.TickProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

interface StateView {
    val n: Int
}

data class State(var n: Int = 0)

data class IncomingDelta(val inc: Int)

fun logic(state: State, delta: IncomingDelta) {
    state.n += delta.inc
}

class IncomingData {
    val data = TreeMap<Long, IncomingDelta>()
    fun hasDelta(t: Long) = data[t] != null
}

class GameStub {
    val incoming = IncomingData()
    private val instructionsProvider = SimulationDirectorImpl(
        incoming::hasDelta,
        object : TickProvider {
            override var tick
                get() = _tick.toLong()
                set(value) = throw UnsupportedOperationException()
        })
    private var authoritativeState: State = State(0)
    private var guessedState: State? = null

    private var _tick = 0

    fun tick() {
        val instructions = instructionsProvider.instruct()
        perform(instructions)
        _tick++
        println("tick: $_tick, n: ${stateView().n}")
    }

    private fun perform(instructions: List<Instruction>) {
        instructions.forEach(::perform)
    }

    private fun perform(instruction: Instruction) {
        when (instruction) {
            is Instruction.IntegrateAuthoritative -> {
                integrateAuthoritative(getDelta(instruction.tick))
            }
            is Instruction.CopyAuthoritativeToGuessed -> {
                copyAuthoritativeToGuessed()
            }
            is Instruction.IntegrateGuessed -> {
                integrateGuessed(getDelta(instruction.tick))
            }
        }
    }

    private fun getDelta(t: Long) = incoming.data[t]!!

    private fun copyAuthoritativeToGuessed() {
        this.guessedState = State(this.authoritativeState.n)
    }

    private fun integrateGuessed(guessedDelta: IncomingDelta) {
        logic(guessedState!!, guessedDelta)
    }


    private fun integrateAuthoritative(delta: IncomingDelta) {
        logic(authoritativeState, delta)
    }

    fun stateView(): StateView {
        if (instructionsProvider.currentState is CurrentState.Authoritative) {
            return object : StateView {
                override val n: Int
                    get() = authoritativeState.n
            }
        } else {
            return object : StateView {
                override val n: Int
                    get() = guessedState!!.n
            }
        }
    }
}

class SimulationDirectorTest {
    lateinit var game: GameStub

    @BeforeEach
    fun setUp() {
        game = GameStub()
    }

    @Test
    fun `should apply incoming delta`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        assert(game.stateView().n == 1)
    }

    @Test
    fun `should guess if data is missed`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        assertEquals(2, game.stateView().n)
    }

    @Test
    fun `should fix if data arrived`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        game.incoming.data[1] = IncomingDelta(2)
        game.incoming.data[2] = IncomingDelta(2)
        game.tick()
        assertEquals(5, game.stateView().n)
    }

    @Test
    fun `and then should guess again if needed`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        game.incoming.data[1] = IncomingDelta(2)
        game.incoming.data[2] = IncomingDelta(2)
        game.tick()
        game.tick()
        assertEquals(7, game.stateView().n)
    }

    @Test
    fun `should handle two missing packets with ease`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        game.tick()
        game.tick()
        assertEquals(4, game.stateView().n)
    }

    @Test
    fun `should catch up as much as it can and adjust the guess`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        game.tick()
        game.tick()
        assertEquals(4, game.stateView().n)
        game.incoming.data[1] = IncomingDelta(2)
        game.tick()
        assertEquals(11, game.stateView().n)
    }

    @Test
    fun `should catch up as much as it can and adjust the guess with different tail`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        game.tick()
        game.tick()
        assertEquals(4, game.stateView().n)
        game.incoming.data[1] = IncomingDelta(2)
        game.incoming.data[2] = IncomingDelta(3)
        game.tick()
        assertEquals(15, game.stateView().n)
    }

    @Test
    fun `should catch up as much as it can and adjust the guess with different tail 2`() {
        game.incoming.data[0] = IncomingDelta(1)
        game.tick()
        game.tick()
        game.tick()
        game.tick()
        assertEquals(4, game.stateView().n)
        game.tick()
        game.incoming.data[1] = IncomingDelta(2)
        game.incoming.data[2] = IncomingDelta(3)
        game.tick()
        assertEquals(18, game.stateView().n)
    }
}
