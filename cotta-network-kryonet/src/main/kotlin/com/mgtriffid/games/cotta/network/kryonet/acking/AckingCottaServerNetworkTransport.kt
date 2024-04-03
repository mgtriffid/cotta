package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.io.Output
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.server.KryonetCottaServerNetworkTransport
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

private val logger = KotlinLogging.logger {}

// TODO more flexible configuration: allow larger datagrams through config,
//  allow more chunks than 256, etc.
class AckingCottaServerNetworkTransport(
    private val impl: KryonetCottaServerNetworkTransport
) : CottaServerNetworkTransport by impl {
    private val connections = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build<ConnectionId, Connection>(CacheLoader.from { _ -> Connection() })

    override fun send(connectionId: ConnectionId, obj: Any) {
        val output = Output(1024 * 1024)
        logger.info { "Sending $obj" }
        impl.kryo.writeClassAndObject(output, obj)
        val bytes = output.toBytes()
        logger.info { "Bytes size is ${bytes.size}" }
        val connection = connections[connectionId]
        val packetSequence = connection.packetSequence
        val objectId = connection.track(obj)
        val chunked = bytes.chunked()
        val size = chunked.size
        logger.info { " $size chunks" }
        if (size > 128) {
            throw IllegalArgumentException("Too many chunks")
        }
        val squadronId = SquadronId(connection.packetSequence.get())

        chunked.forEachIndexed { idx, b ->
            impl.send(connectionId, Chunk().apply {
                this.squadron = squadronId.id
                packet = idx.toByte()
                this.size = size.toByte()
                data = b
            }
                .also { "Sending chunk of squadron ${it.squadron} and packet ${it.packet}" }
            )
        }
        packetSequence.addAndGet(size)
        connection.markSent(squadronId, setOf(objectId), size)
    }

    private fun ByteArray.chunked(): List<ByteArray> {
        val chunks =
            size / DATAGRAM_SIZE + (if (size % DATAGRAM_SIZE == 0) 0 else 1)
        return (0 until chunks).map { i ->
            val start = i * DATAGRAM_SIZE
            val end = min(size, (i + 1) * DATAGRAM_SIZE)
            sliceArray(start until end)
        }
    }
}

// TODO optimize the hell out of it
class Connection {
    private var objectSequence = 0
    private val objectsInFlight = mutableMapOf<ObjectId, Any>()
    private val squadronToObjects =
        mutableMapOf<SquadronId, MutableSet<ObjectId>>()
    private val objectToSquadrons =
        mutableMapOf<ObjectId, MutableSet<SquadronId>>()

    private val squadronToPackets = TreeMap<SquadronId, Squadron>()
    val packetSequence: AtomicInteger = AtomicInteger()

    fun track(obj: Any): ObjectId {
        val objectId = ObjectId(objectSequence++)
        objectsInFlight[objectId] = obj
        return objectId
    }

    fun markSent(squadronId: SquadronId, objects: Set<ObjectId>, size: Int) {
        squadronToObjects[squadronId] = objects.toMutableSet()
        squadronToPackets[squadronId] = Squadron(
            size = size,
            packets = (0 until size).map { PacketId(it + squadronId.id) }
                .toMutableSet()
        )
        objects.forEach {
            objectToSquadrons.computeIfAbsent(it) { mutableSetOf() }
                .add(squadronId)
        }
    }

    fun confirm(packetId: PacketId) {
        val (k, v) = squadronToPackets.floorEntry(SquadronId(packetId.id))
            ?: return
        if (packetId !in v.packets) {
            return
        }
        v.packets.remove(packetId)
        if (v.packets.isEmpty()) {
            // squadron sent completely
            squadronToPackets.remove(k)
            // objects that are guaranteed to be delivered:
            val objects = squadronToObjects.remove(k)!!
            objects.forEach { objectId ->
                // remove all squadrons:
                val squadrons = objectToSquadrons.remove(objectId)
                // remove this object from all squadrons:
                squadrons?.forEach { squadronId ->
                    val objectsInSquadron = squadronToObjects[squadronId]
                    objectsInSquadron?.remove(objectId)
                    if (objectsInSquadron?.isEmpty() == true) {
                        squadronToObjects.remove(squadronId)
                        squadronToPackets.remove(squadronId)
                    }
                }
                objectsInFlight.remove(objectId)
            }
        }
    }

    fun objectsInFlight(): Set<Any> {
        return objectsInFlight.values.toSet()
    }

    fun squadronsInFlight(): Int {
        return squadronToPackets.size
    }
}

@JvmInline
value class SquadronId(val id: Int) : Comparable<SquadronId> {
    override fun compareTo(other: SquadronId): Int {
        return id.compareTo(other.id)
    }
}

@JvmInline
value class ObjectId(val id: Int)

@JvmInline
value class PacketId(val id: Int)

class Squadron(
    val size: Int,
    val packets: MutableSet<PacketId>
)
