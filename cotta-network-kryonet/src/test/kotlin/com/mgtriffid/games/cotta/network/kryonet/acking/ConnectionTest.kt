package com.mgtriffid.games.cotta.network.kryonet.acking

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectionTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setUp() {
        connection = Connection(
            serializer = StubSerializer,
            sendChunk = { },
            saveObject = { }
        )
    }

    @Test
    fun testSimpleTracking() {
        val obj = "Obj"
        val objectId = connection.track(obj)
        connection.markSent(SquadronId(1), setOf(objectId), 1)
        assertEquals(obj, connection.objectsInFlight().first().obj)
    }

    @Test
    fun testSimpleConfirmation() {
        val obj = "Obj"
        val objectId = connection.track(obj)
        connection.markSent(SquadronId(1), setOf(objectId), 1)
        connection.confirm(PacketId(1))
        assertEquals(emptySet<String>(), connection.objectsInFlight())
    }

    @Test
    fun `should be in flight when one packet confirmed but not another`() {
        val obj = "Obj"
        val objectId = connection.track(obj)
        connection.markSent(SquadronId(1), setOf(objectId), 2)
        connection.confirm(PacketId(2))
        assertEquals(obj, connection.objectsInFlight().first().obj)
    }

    @Test
    fun `should be confirmed when both packets confirmed`() {
        val obj = "Obj"
        val objectId = connection.track(obj)
        connection.markSent(SquadronId(1), setOf(objectId), 2)
        connection.confirm(PacketId(1))
        connection.confirm(PacketId(2))
        assertEquals(emptySet<Any>(), connection.objectsInFlight())
    }

    @Test
    fun `should confirm the same packet twice without error`() {
        val obj = "Obj"
        val objectId = connection.track(obj)
        connection.markSent(SquadronId(1), setOf(objectId), 1)
        connection.confirm(PacketId(1))
        connection.confirm(PacketId(1))
        assertEquals(emptySet<Any>(), connection.objectsInFlight())
    }

    @Test
    fun `should forget a squadron if all objects confirmed via other squadrons`() {
        val obj1 = "Obj1"
        val obj2 = "Obj2"
        val obj3 = "Obj3"
        val obj4 = "Obj4"
        val objectId1 = connection.track(obj1)
        val objectId2 = connection.track(obj2)
        val objectId3 = connection.track(obj3)
        val objectId4 = connection.track(obj4)
        connection.markSent(SquadronId(1), setOf(objectId1, objectId2), 2)
        connection.markSent(SquadronId(3), setOf(objectId2, objectId3), 2)
        connection.markSent(SquadronId(5), setOf(objectId3, objectId4), 2)
        connection.confirm(PacketId(1))
        connection.confirm(PacketId(2))
        connection.confirm(PacketId(5))
        connection.confirm(PacketId(6))

        assertEquals(0, connection.squadronsInFlight())
    }
}

private object StubSerializer : ChunkSerializer {
    override fun serialize(objs: ArrayList<Any>): ByteArray {
        return objs.toString().toByteArray()
    }

    override fun deserialize(bytes: ByteArray): ArrayList<*> {
        return arrayListOf(String(bytes))
    }
}
