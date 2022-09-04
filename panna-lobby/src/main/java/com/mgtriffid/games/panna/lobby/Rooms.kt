package com.mgtriffid.games.panna.lobby

class Rooms {
    val rooms: MutableMap<RoomId, Room> = HashMap()
    init {
        rooms[RoomId("room1")] = Room("Playgrounds")
        rooms[RoomId("room2")] = Room("ZTN")
        rooms[RoomId("room3")] = Room("Aerowalk")
    }
}

data class RoomId(val roomId: String)

data class Room(
    val map: String
)
