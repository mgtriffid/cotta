package com.mgtriffid.games.panna.lobby

import java.util.UUID

class Rooms {
    val rooms: MutableMap<RoomId, Room> = HashMap()
    init {
        rooms[RoomId(UUID.randomUUID().toString())] = Room("Playgrounds")
        rooms[RoomId(UUID.randomUUID().toString())] = Room("ZTN")
        rooms[RoomId(UUID.randomUUID().toString())] = Room("Aerowalk")
    }
}

data class RoomId(val roomId: String)

data class Room(
    val map: String
)
