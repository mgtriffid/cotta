package com.mgtriffid.games.cotta.network.kryonet.acking

import com.google.common.cache.CacheBuilder
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

// TODO optimize the hell out of it
class Connection(
    private val serializer: ChunkSerializer,
    private val sendChunk: (Chunk) -> Unit,
    private val saveObject: (Any) -> Unit
) {
    private val receivedPackets = ReceivedPackets()
    private var objectSequence = 0
    private val objectsInFlight = mutableMapOf<ObjectId, ObjectInFlight>()

    private val squadronsInFlight = TreeMap<SquadronId, Squadron>()
    val packetSequence: AtomicInteger = AtomicInteger()

    private val incomingSquadrons = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build<SquadronId, IncomingSquadron>()

    fun send(obj: Any) {
        synchronized(this) {
            logger.trace { "In flight: ${objectsInFlight.entries.map { (k, v) -> "$k -> ${v.obj.javaClass.simpleName}" }}" }
            val packetSequence = packetSequence
            track(obj)
            val objectsToSent = getLastObjectsInFlight() // this
            val bytes = serializer.serialize(ArrayList(objectsToSent.values)) // this
            logger.trace { "Bytes size is ${bytes.size}" }
            val chunked = bytes.chunked()
            val size = chunked.size
            logger.trace { " $size chunks" }
            if (size > 128) {
                throw IllegalArgumentException("Too many chunks")
            }
            val squadronId = SquadronId(packetSequence.get())

            chunked.forEachIndexed { idx, b ->
                sendChunk(Chunk().apply {
                    this.acks = Acks().apply {// track how many not confirmed?
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
            markSent(squadronId, objectsToSent.keys, size)
        }
    }

    fun track(obj: Any): ObjectId {
        val objectId = ObjectId(objectSequence++)
        objectsInFlight[objectId] = ObjectInFlight(obj, mutableSetOf()) // track when added
        return objectId
    }

    fun markSent(squadronId: SquadronId, objects: Set<ObjectId>, size: Int) {
        squadronsInFlight[squadronId] = Squadron(
            packets = (0 until size).map { PacketId(it + squadronId.id) }
                .toMutableSet(),
            objects = objects.toMutableSet()
        )
        objects.forEach {
            objectsInFlight[it]?.squadrons?.add(squadronId) ?: logger.error { "Object $it not found" }
        }
    }

    fun confirm(packetId: PacketId) {
        val (k, v) = squadronsInFlight.floorEntry(SquadronId(packetId.id))
            ?: return
        if (packetId !in v.packets) {
            return
        }
        logger.trace { "Confirmed $packetId" }
        v.packets.remove(packetId)
        if (v.packets.isEmpty()) {
            // squadron sent completely
            squadronsInFlight.remove(k)
            // objects that are guaranteed to be delivered:
            v.objects.forEach { objectId ->
                // remove all squadrons:
                // track when removed - meaningful latency
                val squadrons = objectsInFlight.remove(objectId)?.squadrons
                // remove this object from all squadrons:
                squadrons?.forEach { squadronId ->
                    val squadron = squadronsInFlight[squadronId]
                    squadron?.objects?.remove(objectId)
                    if (squadron?.objects?.isEmpty() == true) {
                        squadronsInFlight.remove(squadronId)
                    }
                }
            }
        }
    }

    fun receiveChunk(chunk: Chunk) {
        synchronized(this) {
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
                serializer.deserialize(bytes).forEach { saveObject(it) }
            }
            processAcks(chunk.acks)
        }
    }

    private fun processAcks(acks: Acks) {
        (0..63).forEachIndexed { idx, bit ->
            if (acks.received and (1L shl bit) != 0L) {
                confirm(PacketId(acks.last - idx))
            }
        }
    }

    private fun getLastObjectsInFlight(): Map<ObjectId, Any> {
        val objects = objectsInFlight.toSortedMap()
        val ids = objects.keys.toList()
        return ids.takeLast(8).associateWith { objects[it]!!.obj }
    }

    fun objectsInFlight(): Set<ObjectInFlight> {
        return objectsInFlight.values.toSet()
    }

    fun squadronsInFlight(): Int {
        return squadronsInFlight.size
    }
}

@JvmInline
value class SquadronId(val id: Int) : Comparable<SquadronId> {
    override fun compareTo(other: SquadronId): Int {
        return id.compareTo(other.id)
    }
}

@JvmInline
value class ObjectId(val id: Int) : Comparable<ObjectId> {
    override fun compareTo(other: ObjectId): Int {
        return id.compareTo(other.id)
    }
}

@JvmInline
value class PacketId(val id: Int)

class Squadron(
    val packets: MutableSet<PacketId>,
    val objects: MutableSet<ObjectId>
)

class ObjectInFlight(
    val obj: Any,
    val squadrons: MutableSet<SquadronId>
)
