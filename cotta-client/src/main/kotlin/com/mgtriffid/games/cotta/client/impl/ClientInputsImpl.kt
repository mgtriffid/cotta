package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientInputs
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.ClientInput
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.*

class ClientInputsImpl @Inject constructor(
    private val tickProvider: TickProvider,
    @Named("clientInputBufferLength") private val bufferLength: Int
) : ClientInputs {
    private val data = Array<Envelope>(bufferLength) { Envelope.Absent }

    // called before advancing tick
    override fun store(input: ClientInput) {
        store(tickProvider.tick, input)
    }

    override fun get(tick: Long): ClientInput {
        val pos = (tick % bufferLength).toInt()
        val envelope = data[pos]
        if (envelope is Envelope.Present && envelope.tick == tick) {
            return envelope.input
        }
        throw IllegalStateException("No input for tick $tick") // TODO think about how to handle this gracefully
    }

    private fun store(tick: Long, input: ClientInput) {
        val pos = (tick % bufferLength).toInt()
        data[pos] = Envelope.Present(tick, input)
    }

    private sealed interface Envelope {
        class Present(val tick: Long, val input: ClientInput) : Envelope
        object Absent : Envelope
    }

    override fun all(): SortedMap<Long, ClientInput> {
        return data.filterIsInstance<Envelope.Present>().associate { it.tick to it.input }.toSortedMap()
    }
}