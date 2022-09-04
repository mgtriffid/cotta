package com.mgtriffid.games.panna.lobby

import com.google.gson.Gson
import spark.Spark.get

class PannaLobby {
    private val rooms: Rooms = Rooms()

    fun start() {
        initializeEndpoints()
    }

    private fun initializeEndpoints() {
        val gson = Gson()
        // rooms/
        // rooms/init
        // rooms/delete (if u own it, also need a web console of any kind for rooms, or adming permissions is fine)
        // auth of any kind on top of it, for now password
        // enter room (returns some specific client id, also makes an entry for this entering player in the room and
        // warms up data that is stored in the database)
        // list players in the room (why?)
        //

        get(
            "/rooms",
            { _, _ -> RoomsDto(items = rooms.rooms.entries.map { RoomDto(it.key.roomId, it.value.map) }) },
            gson::toJson
        )
    }
}

data class RoomsDto(
    val items: List<RoomDto>
)

data class RoomDto(
    val roomId: String,
    val mapId: String
)
