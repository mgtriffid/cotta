package com.mgtriffid.games.cotta.client.impl

import com.esotericsoftware.kryonet.Client
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.*

class LocalPlayerInputsImpl @Inject constructor(
    @Named(GLOBAL) private val tickProvider: TickProvider,
    @Named("clientInputBufferLength") private val bufferLength: Int,
    private val localInput: CottaClientInput
) : LocalPlayerInputs {

    private var inputIdSequence: Int = 0
    private val data = Array<Envelope>(bufferLength) { Envelope.Absent }

    private var lastSent: Int = 0

    override fun get(clientInputId: ClientInputId): PlayerInput {
        val pos = (clientInputId.id % bufferLength)
        val envelope = data[pos]
        if (envelope is Envelope.Present) {
            return envelope.input
        }
        throw IllegalStateException("No input for id $clientInputId") // TODO think about how to handle this gracefully
    }

    private fun store(id: ClientInputId, input: PlayerInput) {
        val pos = (id.id % bufferLength)
        data[pos] = Envelope.Present(id, tickProvider.tick, input)
    }

    override fun all(): SortedMap<ClientInputId, PlayerInput> {
        return data.filterIsInstance<Envelope.Present>().associate { it.id to it.input }.toSortedMap()
    }

    override fun collect() {
        store(
            ClientInputId(++inputIdSequence),
            localInput.input()
        )
    }

    override fun unsent(): List<Triple<ClientInputId, PlayerInput, Long>> {
        return ((lastSent + 1)..inputIdSequence).map {
            var envelope = data[(it % bufferLength)] as Envelope.Present
            Triple(
                ClientInputId(it),
                envelope.input,
                envelope.sawTick
            )
        }.also { lastSent = inputIdSequence }
    }

    private sealed interface Envelope {
        class Present(
            val id: ClientInputId,
            val sawTick: Long,
            val input: PlayerInput
        ) : Envelope
        data object Absent : Envelope
    }
}
