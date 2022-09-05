package com.mgtriffid.games.panna.lobby

import com.google.gson.Gson
import spark.Request
import spark.Response
import spark.Spark
import spark.Spark.get
import spark.Spark.post
import spark.kotlin.halt
import java.util.*


class PannaLobby {
    private val rooms: Rooms = Rooms()
    private val sessions = HashMap<SessionToken, Username>()

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
        Spark.exception(
            Exception::class.java
        ) { exception: Exception, request: Request?, response: Response? -> exception.printStackTrace() }
        post(
            "/login",
            { req, res ->
                val (user, pass) = gson.fromJson(req.body(), LoginDto::class.java)
                if (pass.startsWith(user)) {
                    val token = UUID.randomUUID().toString()
                    sessions[SessionToken(token)] = Username(user)
                    SuccessfulLoginResponse(token)
                } else {
                    halt(401, "Login failed")
                }
            },
            gson::toJson
        )
        get(
            "/rooms",
            { _, _ -> RoomsDto(items = rooms.rooms.entries.map { RoomDto(it.key.roomId, it.value.map) }) },
            gson::toJson
        )
        get(
            "/rooms/:roomId",
            { req, _ -> rooms.rooms[RoomId(req.params("roomId"))]?.let { RoomDto(req.params("roomId"), it.map) } },
            gson::toJson
        )
        post(
            "/rooms",
            { req, res ->
                val body = gson.fromJson(req.body(), CreateRoomDto::class.java)
                val id = UUID.randomUUID().toString()
                rooms.rooms[RoomId(id)] = Room(body.map)
                CreateRoomDtoResponse(id)
            },
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

data class CreateRoomDto(
    val map: String
)

data class CreateRoomDtoResponse(
    val roomId: String
)

data class LoginDto(
    val username: String,
    val password: String
)

data class SessionToken(
    val token: String
)

data class Username(val username: String)

data class SuccessfulLoginResponse(val token: String)
