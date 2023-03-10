package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.network.CottaServerNetwork
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerToClientDataChannel
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerToClientDataChannelImpl<SR: StateRecipe, DR: DeltaRecipe> (
    private val tick: TickProvider,
    private val clientsGhosts: ClientsGhosts,
    private val network: CottaServerNetwork,
    private val stateSnapper: StateSnapper<SR, DR>,
    private val snapsSerialization: SnapsSerialization<SR, DR>
) : ServerToClientDataChannel {

    // Here we don't care much about data that comes _from_ client and about their sawTick values.
    // We do, however, care about data that is being sent _to_ clients. We record acks, we record what entities
    // did clients see and what entities they did not see. We also care about predicted entities of clients. Yes,
    // when client predicts an entity spawn, we should let it know that "ok bro we have acknowledged your prediction,
    // you predicted it as id `predicted_1`, we gave it id `543`, now we match all input you give for that entity,
    // and when we send you this entity back, your job is to start treating `543` as the id, not `predicted_1`.

    override fun send(data: DataForClients) {
        val tick = tick.tick
        clientsGhosts.data.forEach { (playerId, ghost) ->
            val whatToSend = ghost.whatToSend(tick)
            whatToSend.necessaryData.forEach { (tick, kind) ->
                network.send(ghost.connectionId, packData(tick, kind, data))
            }
        }
    }

    private fun packData(tick: Long, kindOfData: KindOfData, data: DataForClients) {
        val snappedData: Any = when (kindOfData) {
            KindOfData.STATE -> stateSnapper.snapState(data.entities(tick))
            KindOfData.DELTA -> stateSnapper.snapDelta(prev = data.entities(tick - 1), curr = data.entities(tick))
        }
    }
}
