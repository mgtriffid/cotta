package com.mgtriffid.games.cotta.core.serialization.bytes

object ConversionUtils {
    fun writeInt(bytes: ByteArray, value: Int, offset: Int) {
        bytes[offset] = (value ushr 24).toByte()
        bytes[offset + 1] = (value shr 16).toByte()
        bytes[offset + 2] = (value shr 8).toByte()
        bytes[offset + 3] = value.toByte()
    }

    fun readInt(byteArray: ByteArray, offset: Int): Int {
        var i = 0
        for (j in 0..3) {
            val shift = 24 - j * 8
            i += (byteArray[offset + j].toInt() and 0xFF) shl shift
        }
        return i
    }
}

