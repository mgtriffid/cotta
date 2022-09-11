package com.mgtriffid.games.panna.lobby

import java.util.UUID

class Rooms {
    val rooms: MutableMap<RoomId, Room> = HashMap()
    init {
        rooms[RoomId(UUID.randomUUID().toString())] = Room("Playgrounds", RoomOwner.Server)
        rooms[RoomId(UUID.randomUUID().toString())] = Room("ZTN", RoomOwner.Server)
        rooms[RoomId(UUID.randomUUID().toString())] = Room("Aerowalk", RoomOwner.Server)
    }
}

data class RoomId(val roomId: String)

data class Room(
    val map: String,
    val owner: RoomOwner
)

sealed interface RoomOwner {
    object Server: RoomOwner
    class Client(val username: Username): RoomOwner
}
