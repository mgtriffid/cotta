package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.network.protocol.serialization.ServerToClientGameDataPacket
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.ServerToClientDataChannel

class ServerToClientDataChannelImpl(
    private val tick: TickProvider,
    private val clientsGhosts: ClientsGhosts
) : ServerToClientDataChannel {

    // Here we don't care much about data that comes _from_ client and about their sawTick values.
    // We do, however, care about data that is being sent _to_ clients. We record acks, we record what entities
    // did clients see and what entities they did not see. We also care about predicted entities of clients. Yes,
    // when client predicts an entity spawn, we should let it know that "ok bro we have acknowledged your prediction,
    // you predicted it as id `predicted_1`, we gave it id `543`, now we match all input you give for that entity,
    // and when we send you this entity back, your job is to start treating `543` as the id, not `predicted_1`.

    override fun send(data: DataForClients) {
        val tick = tick.tick
        clientsGhosts.data.forEach {
            it.value.send(data, tick)
        }
        actuallySendData(clientsGhosts)
    }

    private fun actuallySendData(clientGhosts: ClientsGhosts) {
        clientGhosts.data.forEach { (_, ghost) ->
            val packets = ghost.drainQueue()
            packets.map {
                when (it) {
                    is ServerToClientGameDataPacket.StatePacket -> {
                        //language=JSON
                        """{
  "entities": [
    {
      "entityId": "123",
      "components": [
        {
          "name": "PositionComponent",
          "data": {
            "x": 10,
            "y": 20
          }
        }
      ]
    }
  ]
}
                         """
                    }
                    is ServerToClientGameDataPacket.DeltaPacket -> {
                        //language=JSON5
                        """{
                            "removedEntityIds": [123, 456, 789],
                            "addedEntities": [
    {
      "entityId": "123",
      "components": [
        {
          "name": "PositionComponent",
          "data": {
            "x": 10,
            "y": 20
          }
        }
      ]
    }
  ],
  "modifiedEntities": [
  {"entityId": "8", "components": [
    {
      "name": "PositionComponent",
      "change": "MODIFIED", // or "ADDED" or "REMOVED"
      "data": {
        "x": "12"
      }
    }
  ]}
  ]
}
                        """.trimIndent()
                    }
                }
            }

        }
    }
}
