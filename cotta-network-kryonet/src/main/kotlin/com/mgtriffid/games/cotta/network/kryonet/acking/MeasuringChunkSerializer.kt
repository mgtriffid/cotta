package com.mgtriffid.games.cotta.network.kryonet.acking

import com.codahale.metrics.Histogram
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MeasuringChunkSerializer(
    private val impl: ChunkSerializer,
    private val sizeHistogram: Histogram
) : ChunkSerializer {
    override fun serialize(objs: ArrayList<Any>): ByteArray {
        return impl.serialize(objs).also(::recordSize)
    }

    override fun deserialize(bytes: ByteArray): ArrayList<*> {
        return impl.deserialize(bytes)
    }

    private fun recordSize(bytes: ByteArray) {
        val size = bytes.size.toLong()
        sizeHistogram.update(size)
        logger.debug { "Recorded size: $size" }
    }
}
