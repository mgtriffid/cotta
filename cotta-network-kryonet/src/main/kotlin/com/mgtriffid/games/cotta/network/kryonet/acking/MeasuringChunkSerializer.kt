package com.mgtriffid.games.cotta.network.kryonet.acking

import com.codahale.metrics.Histogram

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
        sizeHistogram.update(bytes.size.toLong())
    }
}
