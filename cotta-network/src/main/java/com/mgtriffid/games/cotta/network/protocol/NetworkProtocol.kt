package com.mgtriffid.games.cotta.network.protocol

data class ClientToServerEnvelope(
    val identity: IdentityInfoDto,
    val payload: ClientToServerPayload
)

sealed class ClientToServerPayload {

    object EnterTheGame: ClientToServerPayload()

    data class InGameDataDto(
        val input: PlayerInputDto,
        val tickSeen: TickDto,
        val acks: AcksDto // perhaps this should be named differently and accommodate "give me the freshest state" too.
    )
}

data class IdentityInfoDto(
    val todo: String = "keke"
)

data class TickDto(val tick: Long)

data class PlayerInputDto(
    val todo: String = "qq"
)

data class AcksDto(
    // potentially may inclide info like "what was the last authoritative state this client received". Because if there
    // was a missed packet 65 ticks ago and we don't see it in this bitmask - it is troubling.
    val bitmask: Long
)
