package com.mgtriffid.games.cotta.network.kryonet.acking

fun ByteArray.chunked(): List<ByteArray> {
    val chunks =
        size / DATAGRAM_SIZE + (if (size % DATAGRAM_SIZE == 0) 0 else 1)
    return (0 until chunks).map { i ->
        val start = i * DATAGRAM_SIZE
        val end = kotlin.math.min(size, (i + 1) * DATAGRAM_SIZE)
        sliceArray(start until end)
    }
}
