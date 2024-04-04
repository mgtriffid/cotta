package com.mgtriffid.games.cotta.network.kryonet.acking

class ReceivedPackets {
    var last = -1
    var received = 0L

    fun markReceived(squadron: Int, chunk: Byte) {
        val packet = squadron + chunk
        if (last < packet) {
            val offset = packet - last
            received = if (offset > 63) {
                0L
            } else {
                received shl offset
            }
            last = packet
        }
        if (last - packet < 63) {
            received = received or (1L shl (last - packet))
        }
    }
}
