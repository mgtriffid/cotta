package com.mgtriffid.games.cotta.core.serialization.bytes

import com.mgtriffid.games.cotta.core.entities.id.EntityId

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

    fun writeLong(bytes: ByteArray, value: Long, offset: Int) {
        bytes[offset] = (value ushr 56).toByte()
        bytes[offset + 1] = (value ushr 48).toByte()
        bytes[offset + 2] = (value ushr 40).toByte()
        bytes[offset + 3] = (value ushr 32).toByte()
        bytes[offset + 4] = (value ushr 24).toByte()
        bytes[offset + 5] = (value shr 16).toByte()
        bytes[offset + 6] = (value shr 8).toByte()
        bytes[offset + 7] = value.toByte()
    }

    fun readLong(byteArray: ByteArray, offset: Int): Long {
        var l = 0L
        for (j in 0..7) {
            val shift = 56 - j * 8
            l += (byteArray[offset + j].toLong() and 0xFF) shl shift
        }
        return l
    }

    fun writeFloat(bytes: ByteArray, value: Float, offset: Int) {
        writeInt(bytes, value.toBits(), offset)
    }

    fun readFloat(byteArray: ByteArray, offset: Int): Float {
        return Float.fromBits(readInt(byteArray, offset))
    }

    fun writeDouble(bytes: ByteArray, value: Double, offset: Int) {
        writeLong(bytes, value.toBits(), offset)
    }

    fun readDouble(byteArray: ByteArray, offset: Int): Double {
        return Double.fromBits(readLong(byteArray, offset))
    }

    fun writeByte(bytes: ByteArray, value: Byte, offset: Int) {
        bytes[offset] = value
    }

    fun readByte(byteArray: ByteArray, offset: Int): Byte {
        return byteArray[offset]
    }

    fun writeShort(bytes: ByteArray, value: Short, offset: Int) {
        bytes[offset] = (value.toInt() ushr 8).toByte()
        bytes[offset + 1] = value.toByte()
    }

    fun readShort(byteArray: ByteArray, offset: Int): Short {
        return ((byteArray[offset].toInt() and 0xFF shl 8) or (byteArray[offset + 1].toInt() and 0xFF)).toShort()
    }

    fun writeEntityId(bytes: ByteArray, entityId: EntityId, offset: Int) {
        when (entityId) {
            is EntityId -> {
                writeInt(bytes, -1, offset)
                writeInt(bytes, entityId.id, offset + 4)
            }
        }
    }

    fun readEntityId(byteArray: ByteArray, offset: Int): EntityId {
        val type = readInt(byteArray, offset)
        return when (type) {
            -1 -> EntityId(readInt(byteArray, offset + 4))
            else -> throw IllegalStateException("Unknown entity id type: $type")
        }
    }
}
