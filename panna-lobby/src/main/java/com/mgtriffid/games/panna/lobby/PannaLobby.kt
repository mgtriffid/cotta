package com.mgtriffid.games.panna.lobby

import com.google.gson.Gson
import com.mgtriffid.games.panna.shared.lobby.SuccessfulLoginResponse
import mu.KotlinLogging
import spark.Request
import spark.Response
import spark.Spark
import spark.Spark.before
import spark.Spark.get
import spark.Spark.post
import spark.Spark.halt
import java.lang.IllegalArgumentException
import java.util.*

private val logger = KotlinLogging.logger {}
/**
 * This is by no means production ready! be sure to replace security with proper mature solution like Spring Security,
 * or Apache Shiro, or something.
 */
class PannaLobby {
    private val rooms: Rooms = Rooms()
    private val users: Users = Users()
    private val characters: Characters = Characters()
    private val sessions = Sessions()

    fun start() {
        logger.info { "Initializing endpoints" }
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
        before(
            "/rooms/*",
            this::authFilter
        )
        before(
            "/rooms",
            this::authFilter
        )
        post(
            "/login",
            { req, _ ->
                val loginDto = gson.fromJson(req.body(), LoginDto::class.java)
                val username = Username(loginDto.username)
                val password = Password(loginDto.password)
                Thread.sleep(1400)
                if (users.auth(username, password)) {
                    val token = UUID.randomUUID().toString()
                    sessions[SessionToken(token)] = username
                    SuccessfulLoginResponse(token)
                } else {
                    halt(401, "Login failed")
                }
            },
            gson::toJson
        )
        get(
            "/rooms",
            { _, _ -> RoomsDto(items = rooms.rooms.entries.map { RoomDto(it.key.roomId, it.value.map, it.value.owner.humanReadable()) }) },
            gson::toJson
        )
        get(
            "/rooms/:roomId",
            { req, _ ->
                rooms.rooms[RoomId(req.params("roomId"))]?.let {
                    RoomDto(req.params("roomId"), it.map, it.owner.humanReadable())
                }
            },
            gson::toJson
        )
        post(
            "/rooms",
            { req, res ->
                val body = gson.fromJson(req.body(), CreateRoomDto::class.java)
                val id = UUID.randomUUID().toString()
                rooms.rooms[RoomId(id)] = Room(body.map, RoomOwner.Client(getUser(req)))
                CreateRoomDtoResponse(id)
            },
            gson::toJson
        )
        get(
            "/characters",
            { req, _ ->
                logger.info { "GET /characters" }
                val username = getUser(req)
                Thread.sleep(1400)
                characters.forUser(username).map { it.toCharacterDto() }
            },
            gson::toJson
        )
    }

    private fun authFilter(req: Request, resp: Response) {
        if (req.headers("token")?.let(::SessionToken)?.let { sessions[it] } == null) {
            halt(401, "Unauthorized")
        }
    }

    private fun getUserOrNull(req: Request): Username? {
        return req.headers("token")?.let(::SessionToken)?.let { sessions[it] }
    }

    private fun getUser(req: Request): Username = getUserOrNull(req)?.also {
        logger.info { "Resolved user '${it.username}'" }
    } ?: throw halt(401, "Unauthorized")
}

private fun PannaCharacter.toCharacterDto() = CharacterDto(
    name = name.name,
    color = ColorDto(color.r, color.g, color.b)
)

data class CharacterDto(val name: String, val color: ColorDto)

data class ColorDto(
    val r: Int,
    val g: Int,
    val b: Int
)

data class RoomsDto(
    val items: List<RoomDto>
)

data class RoomDto(
    val roomId: String,
    val mapId: String,
    val owner: String
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

fun RoomOwner.humanReadable() : String = when (this) {
    RoomOwner.Server -> "Server"
    is RoomOwner.Client -> username.username
}
