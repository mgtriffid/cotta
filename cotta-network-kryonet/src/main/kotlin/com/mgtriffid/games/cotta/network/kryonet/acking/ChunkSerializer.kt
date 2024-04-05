package com.mgtriffid.games.cotta.network.kryonet.acking

interface ChunkSerializer {
    fun serialize(objs: ArrayList<Any>): ByteArray
    fun deserialize(bytes: ByteArray): ArrayList<*>
}
