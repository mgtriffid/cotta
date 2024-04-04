package com.mgtriffid.games.cotta.network.kryonet.acking

import com.esotericsoftware.kryo.io.Input
import com.google.common.cache.CacheBuilder
import com.mgtriffid.games.cotta.network.protocol.ServerToClientDto
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

// TODO optimize the hell out of it
class Connection(
    private val serialize: (Any) -> ByteArray,
    private val deserialize: (ByteArray) -> Any,
    private val sendChunk: (Chunk) -> Unit,
    private val saveObject: (Any) -> Unit
) {
    private val receivedPackets = ReceivedPackets()
    private var objectSequence = 0
    private val objectsInFlight = mutableMapOf<ObjectId, ObjectInFlight>()
    private val squadronToObjects =
        mutableMapOf<SquadronId, MutableSet<ObjectId>>()

    private val squadronToPackets = TreeMap<SquadronId, Squadron>()
    val packetSequence: AtomicInteger = AtomicInteger()

    private val incomingSquadrons = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build<SquadronId, IncomingSquadron>()

    fun send(obj: Any) {
        val bytes = serialize(obj)
        logger.info { "Bytes size is ${bytes.size}" }
        val packetSequence = packetSequence
        val objectId = track(obj)
        val chunked = bytes.chunked()
        val size = chunked.size
        logger.info { " $size chunks" }
        if (size > 128) {
            throw IllegalArgumentException("Too many chunks")
        }
        val squadronId = SquadronId(packetSequence.get())

        chunked.forEachIndexed { idx, b ->
            sendChunk(Chunk().apply {
                this.acks = Acks().apply {
                    last = receivedPackets.last
                    received = receivedPackets.received
                }
                this.squadron = squadronId.id
                packet = idx.toByte()
                this.size = size.toByte()
                data = b
            }
                .also { "Sending chunk of squadron ${it.squadron} and packet ${it.packet}" }
            )
        }
        packetSequence.addAndGet(size)
        markSent(squadronId, setOf(objectId), size)
    }

    fun track(obj: Any): ObjectId {
        val objectId = ObjectId(objectSequence++)
        objectsInFlight[objectId] = ObjectInFlight(obj, mutableSetOf())
        return objectId
    }

    fun markSent(squadronId: SquadronId, objects: Set<ObjectId>, size: Int) {
        squadronToObjects[squadronId] = objects.toMutableSet()
        squadronToPackets[squadronId] = Squadron(
            packets = (0 until size).map { PacketId(it + squadronId.id) }
                .toMutableSet()
        )
        objects.forEach {
            objectsInFlight[it]?.squadrons?.add(squadronId) ?: logger.error { "Object $it not found" }
        }
    }

    fun confirm(packetId: PacketId) {
        val (k, v) = squadronToPackets.floorEntry(SquadronId(packetId.id))
            ?: return
        if (packetId !in v.packets) {
            return
        }
        logger.info { "Confirmed $packetId" }
        v.packets.remove(packetId)
        if (v.packets.isEmpty()) {
            // squadron sent completely
            squadronToPackets.remove(k)
            // objects that are guaranteed to be delivered:
            val objects = squadronToObjects.remove(k)!!
            objects.forEach { objectId ->
                // remove all squadrons:
                val squadrons = objectsInFlight.remove(objectId)?.squadrons
                // remove this object from all squadrons:
                squadrons?.forEach { squadronId ->
                    val objectsInSquadron = squadronToObjects[squadronId]
                    objectsInSquadron?.remove(objectId)
                    if (objectsInSquadron?.isEmpty() == true) {
                        squadronToObjects.remove(squadronId)
                        squadronToPackets.remove(squadronId)
                    }
                }
            }
        }
    }

    fun receiveChunk(chunk: Chunk) {
        val squadron = incomingSquadrons.get(SquadronId(chunk.squadron)) {
            IncomingSquadron(Array(chunk.size.toInt()) { null })
        }
        receivedPackets.markReceived(chunk.squadron, chunk.packet)
        squadron.data[chunk.packet.toInt()] = chunk.data
        if (squadron.data.all { it != null }) {
            // TODO optimize: allocate in advance, then copy, not copy-copy-copy
            val bytes = squadron.data.fold(ByteArray(0)) { acc, bytes ->
                acc + bytes!!
            }
            val dto = deserialize(bytes)
            saveObject(dto)
        }
        processAcks(chunk.acks)
    }

    private fun processAcks(acks: Acks) {
        (0..63).forEachIndexed { idx, bit ->
            if (acks.received and (1L shl bit) != 0L) {
                confirm(PacketId(acks.last - idx))
            }
        }
    }

    fun objectsInFlight(): Set<ObjectInFlight> {
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
    val packets: MutableSet<PacketId>
)

class ObjectInFlight(
    val obj: Any,
    val squadrons: MutableSet<SquadronId>
)
