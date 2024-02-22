package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientInputs
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.ClientInput
import com.mgtriffid.games.cotta.core.input.impl.ClientInputImpl
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

class ClientInputsImpl @Inject constructor(
    private val tickProvider: TickProvider,
    @Named("clientInputBufferLength") private val bufferLength: Int,
    private val localInput: CottaClientInput
) : ClientInputs {
    private val data = Array<Envelope>(bufferLength) { Envelope.Absent }

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
        data object Absent : Envelope
    }

    override fun all(): SortedMap<Long, ClientInput> {
        return data.filterIsInstance<Envelope.Present>().associate { it.tick to it.input }.toSortedMap()
    }

    override fun collect(
        entities: List<Entity>
    ) {
        store(
            tickProvider.tick,
            ClientInputImpl(
                entities.associate { entity ->
                    entity.id to getInputs(entity)
                }
            )
        )
    }

    private fun getInputs(entity: Entity) = entity.inputComponents().map { clazz ->
        logger.debug { "Retrieving input of class '${clazz.simpleName}' for entity '${entity.id}'" }
        localInput.input(entity, clazz)
    }
}
