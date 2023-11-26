package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.protocol.ClientToServerCreatedPredictedEntitiesDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto

interface CottaClientNetwork {
    fun initialize()

    fun sendEnterGameIntent()

    fun drainIncomingData(): Collection<ServerToClientDto>

    fun sendInput(input: ClientToServerInputDto)

    fun sendCreatedEntities(createdEntitiesDto: ClientToServerCreatedPredictedEntitiesDto)
}
