package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.PlayerInput
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.*

class LocalPlayerInputsImpl @Inject constructor(
    @Named(GLOBAL) private val tickProvider: TickProvider,
    @Named("clientInputBufferLength") private val bufferLength: Int,
    private val localInput: CottaClientInput
) : LocalPlayerInputs {
    private val data = Array<Envelope>(bufferLength) { Envelope.Absent }

    override fun get(tick: Long): PlayerInput {
        val pos = (tick % bufferLength).toInt()
        val envelope = data[pos]
        if (envelope is Envelope.Present && envelope.tick == tick) {
            return envelope.input
        }
        throw IllegalStateException("No input for tick $tick") // TODO think about how to handle this gracefully
    }

    private fun store(tick: Long, input: PlayerInput) {
        val pos = (tick % bufferLength).toInt()
        data[pos] = Envelope.Present(tick, input)
    }

    override fun all(): SortedMap<Long, PlayerInput> {
        return data.filterIsInstance<Envelope.Present>().associate { it.tick to it.input }.toSortedMap()
    }

    override fun collect() {
        store(
            tickProvider.tick,
            localInput.input()
        )
    }

    private sealed interface Envelope {
        class Present(val tick: Long, val input: PlayerInput) : Envelope
        data object Absent : Envelope
    }
}
